/* ==========================================================================
   app.js
   소셜다모아 통합 Frontend Controller
   ========================================================================== */

/* --------------------------------------------------------------------------
   1. Config
   -------------------------------------------------------------------------- */
   const PLATFORM_CONFIG = {
     instagram: { label: 'INSTAGRAM', color: '#E1306C', actionType: 'publish', actionLabel: '즉시 발행' },
     facebook:  { label: 'FACEBOOK',  color: '#1877F2', actionType: 'publish', actionLabel: '즉시 발행' },
     naver:     { label: 'BLOG',      color: '#03C75A', actionType: 'copy',    actionLabel: '복사 후 발행' },
     blog:      { label: 'BLOG',      color: '#03C75A', actionType: 'copy',    actionLabel: '복사 후 발행' },
     kakao:     { label: 'KAKAO',     color: '#FEE500', actionType: 'copy',    actionLabel: '복사 후 발행' },
     community: { label: 'COMMUNITY', color: '#6366F1', actionType: 'copy',    actionLabel: '복사 후 발행' }
   };

const PASTEL_PALETTE = {
  instagram: { backgroundColor: '#fce4ec', textColor: '#880e4f', borderColor: '#e1306c' },
  facebook: { backgroundColor: '#e3f0fd', textColor: '#0d47a1', borderColor: '#1877f2' },
  naver: { backgroundColor: '#e6f9ee', textColor: '#1b5e20', borderColor: '#03c75a' },
  blog: { backgroundColor: '#e6f9ee', textColor: '#1b5e20', borderColor: '#03c75a' },
  kakao: { backgroundColor: '#fffde7', textColor: '#4a3000', borderColor: '#fee500' },
  community: { backgroundColor: '#ede9fe', textColor: '#3730a3', borderColor: '#6366f1' }
};

/* --------------------------------------------------------------------------
   2. State
   -------------------------------------------------------------------------- */
   const state = {
     uploadedImages: [],
     uploadedFiles: [],
     uploadedImageUrl: null,
     communityTags: [],
     currentFilter: 'none',
     appliedFilter: 'none',
     generatedContent: {},
     scheduledEvents: [],
     pendingPosts: [],
     calendarInstance: null
   };

/* --------------------------------------------------------------------------
   3. API Service
   -------------------------------------------------------------------------- */
const apiService = {
  async fetchDashboardData() {
    try {
      const eventsRes = await fetch('/api/posts/events');
      const events = eventsRes.ok ? await eventsRes.json() : [];

      const pendingRes = await fetch('/api/posts/pending');
      const pending = pendingRes.ok ? await pendingRes.json() : [];

      return { events, pending };
    } catch (err) {
      console.error('[App] fetchDashboardData 오류:', err);
      return { events: [], pending: [] };
    }
  },

  async requestGeneratedContents(payload) {
    const response = await fetch('/api/posts/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error(`서버 오류: ${response.status}`);
    }

    return response.json();
  },

  async publishContent(sns, text, filesArray, externalUrlsArray) {
    const formData = new FormData();
    formData.append('platform', sns);
    formData.append('text', text);

    /* 로컬 파일 다중 전송 */
    if (filesArray && filesArray.length > 0) {
      filesArray.forEach(file => {
        formData.append('images', file);
      });
    }

    /* Pexels 및 히스토리 외부 URL 다중 전송 */
    if (externalUrlsArray && externalUrlsArray.length > 0) {
      externalUrlsArray.forEach(url => {
        formData.append('externalUrls', url);
      });
    }

    const response = await fetch('/api/posts/publish', {
      method: 'POST',
      body: formData 
    });

    if (!response.ok) {
      throw new Error(`발행 서버 통신 오류: ${response.status}`);
    }
    return response.json();
  }
  };

/* --------------------------------------------------------------------------
   4. UI Manager
   -------------------------------------------------------------------------- */
const uiManager = {
  showToast(message) {
    alert(message);
  },

  toggleLoading(isLoading) {
    const btn = document.getElementById('generateBtn');
    const progress = document.getElementById('genProgressBar');
    if (!btn) return;

    btn.classList.toggle('loading', isLoading);
    progress?.classList.toggle('active', isLoading);
  },

  updatePreview(sns) {
    const resultBox = document.getElementById(`result-${sns}`);
    const emptyBox = document.getElementById(`empty-${sns}`);

    if (resultBox) resultBox.style.display = 'block';
    if (emptyBox) emptyBox.style.display = 'none';
  },

  createStreamRenderer(textEl, cursorEl) {
    textEl.innerHTML = '';
    textEl.appendChild(cursorEl);

    return (char) => {
      const textNode = document.createTextNode(char);
      textEl.insertBefore(textNode, cursorEl);
    };
  },

  openEventModal(info) {
    const modal = document.getElementById('previewModal');
    if (!modal) return;

    const props = info.event.extendedProps;
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');

    if (modalTitle) modalTitle.textContent = info.event.title;
    if (modalBody) {
      modalBody.innerHTML = (props.bodyText || '내용이 없습니다.').replace(/\n/g, '<br>');
    }

    const currentPost = {
      sns: normalizePlatformKey(props.sns || props.platform || '')
    };

    modal.dataset.sns = currentPost.sns;
    updatePublishButtonByPlatform(currentPost);

    modal.classList.remove('hidden');
  },

  closeEventModal() {
    document.getElementById('previewModal')?.classList.add('hidden');
  }
};

window.openEventModal = uiManager.openEventModal;
window.closeEventModal = uiManager.closeEventModal;






/* 플랫폼별 슬라이드 인덱스 관리 상태 */
const carouselState = {};

/* 다중 이미지 슬라이드(캐러셀) DOM 생성기 */
function createCarouselElement(sns, imageUrls) {
    if (!Array.isArray(imageUrls)) {
        imageUrls = [imageUrls].filter(Boolean);
    }
    if (imageUrls.length === 0) return null;

    carouselState[sns] = 0;

    const wrapper = document.createElement('div');
    wrapper.className = 'sns-carousel-wrapper';
    wrapper.style.cssText = 'position: relative; width: 100%; overflow: hidden; border-radius: 12px; margin-bottom: 15px; background: #f1f5f9; aspect-ratio: 1/1;';

    const track = document.createElement('div');
    track.className = 'sns-carousel-track';
    track.style.cssText = 'display: flex; height: 100%; transition: transform 0.3s ease-in-out;';

    imageUrls.forEach(url => {
        const img = document.createElement('img');
        img.src = url;
        img.style.cssText = 'width: 100%; height: 100%; flex-shrink: 0; object-fit: cover;';
        track.appendChild(img);
    });

    wrapper.appendChild(track);

    /* 2장 이상일 경우 좌우 넘기기 버튼 및 인디케이터(점) 생성 */
    if (imageUrls.length > 1) {
        const prevBtn = document.createElement('button');
        prevBtn.innerHTML = '&#10094;';
        prevBtn.style.cssText = 'position: absolute; left: 10px; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 30px; height: 30px; cursor: pointer; display: none; align-items: center; justify-content: center; z-index: 2;';

        const nextBtn = document.createElement('button');
        nextBtn.innerHTML = '&#10095;';
        nextBtn.style.cssText = 'position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.5); color: white; border: none; border-radius: 50%; width: 30px; height: 30px; cursor: pointer; display: flex; align-items: center; justify-content: center; z-index: 2;';

        const dotsWrap = document.createElement('div');
        dotsWrap.style.cssText = 'position: absolute; bottom: 12px; left: 50%; transform: translateX(-50%); display: flex; gap: 6px; z-index: 2;';
        
        const dots = [];
        imageUrls.forEach((_, idx) => {
            const dot = document.createElement('span');
            dot.style.cssText = `width: 6px; height: 6px; border-radius: 50%; background: ${idx === 0 ? '#fff' : 'rgba(255,255,255,0.5)'}; transition: background 0.3s;`;
            dotsWrap.appendChild(dot);
            dots.push(dot);
        });

        const updateCarouselUI = () => {
            const idx = carouselState[sns];
            track.style.transform = `translateX(-${idx * 100}%)`;
            prevBtn.style.display = idx === 0 ? 'none' : 'flex';
            nextBtn.style.display = idx === imageUrls.length - 1 ? 'none' : 'flex';
            dots.forEach((dot, i) => {
                dot.style.background = i === idx ? '#fff' : 'rgba(255,255,255,0.5)';
            });
        };

        prevBtn.onclick = (e) => {
            e.preventDefault();
            if (carouselState[sns] > 0) {
                carouselState[sns]--;
                updateCarouselUI();
            }
        };

        nextBtn.onclick = (e) => {
            e.preventDefault();
            if (carouselState[sns] < imageUrls.length - 1) {
                carouselState[sns]++;
                updateCarouselUI();
            }
        };

        wrapper.appendChild(prevBtn);
        wrapper.appendChild(nextBtn);
        wrapper.appendChild(dotsWrap);
    }

    return wrapper;
}

/* --------------------------------------------------------------------------
   5. Helpers
   -------------------------------------------------------------------------- */
function getSelectedKeywords() {
  return [...document.querySelectorAll('#keywordGroup input:checked')].map((el) => el.value);
}

function getActiveTones() {
  return [...document.querySelectorAll('#toneGroup .tone-item.active')].map(
    (el) => el.dataset.tone
  );
}

function getActiveEmojiLevel() {
  return document.querySelector('#emojiGroup .slider-item.active')?.dataset.emoji || 'mid';
}

function getActiveSNS() {
  return [...document.querySelectorAll('#snsChips .chip.active')].map((chip) => chip.dataset.sns);
}

function normalizePlatformKey(value) {
  return String(value || '').trim().toLowerCase();
}

function buildGenerateRequestParams() {
  const menu = document.getElementById('menuName')?.value.trim() || '';
  const extra = document.getElementById('extraInfo')?.value.trim() || '';
  const keywords = getSelectedKeywords();
  const activeSNS = getActiveSNS();

  return {
    menu,
    extra,
    keywords,
    activeSNS,
    payload: {
      menuName: menu,
      extraInfo: extra,
      keywords,
      platforms: activeSNS.join(','),
      tones: getActiveTones().join(','),
      emojiLevel: getActiveEmojiLevel(),
      maxLength: document.getElementById('lengthRange') ? parseInt(document.getElementById('lengthRange').value, 10) : 300,
      imageUrl: state.uploadedImageUrl /* 이미지 URL 필드 추가 */
    }
  };
}

function validateGenerateRequest({ menu, keywords, activeSNS }) {
  if (!menu) {
    uiManager.showToast('필수 값(메뉴 / 상품명)을 입력해주세요.');
    return false;
  }
  if (!keywords.length) {
    uiManager.showToast('추가 정보 키워드를 1개 이상 선택해주세요.');
    return false;
  }
  if (!activeSNS.length) {
    uiManager.showToast('updatePlatformVisibility시할 SNS를 1개 이상 선택해 주세요.');
    return false;
  }
  return true;
}

function mergePendingPosts(pendingItems) {
  if (!pendingItems?.length) return;

  const existingIds = new Set(state.pendingPosts.map((post) => String(post.id)));

  pendingItems.forEach((item) => {
    if (existingIds.has(String(item.id))) return;

    const sns = normalizePlatformKey(item.platform);
    state.pendingPosts.push({
      id: item.id,
      sns,
      menu: item.title,
      bodyText: item.content || '',
      label: PLATFORM_CONFIG[sns]?.label || item.platform || '미분류',
      color: item.borderColor || PASTEL_PALETTE[sns]?.borderColor || '#6366F1'
    });
  });
}

function updatePlatformVisibility() {
  const platforms = ['instagram', 'facebook', 'naver', 'kakao', 'community'];
  let firstVisible = null;

  platforms.forEach(sns => {
    const pane = document.getElementById(`pane-${sns}`);
    // Fix: data-target 속성이 아닌 data-tab 속성을 참조하도록 수정
    const tab = document.querySelector(`.tab-btn[data-tab="${sns}"]`);
    
    const hasContent = state.generatedContent && state.generatedContent[sns];

    if (hasContent) {
      if (tab) {
        tab.style.display = 'flex';
        tab.classList.remove('active'); // 초기화
      }
      if (pane) {
        pane.style.display = ''; // 인라인 블록 강제 속성 제거
        pane.classList.remove('active'); // 초기화
      }
      if (!firstVisible) firstVisible = sns;
    } else {
      if (tab) tab.style.display = 'none';
      if (pane) {
        pane.style.display = 'none';
        pane.classList.remove('active');
      }
    }
  });

  // Fix: 컨텐츠가 하단에 쌓이지 않고 첫 번째 활성 탭만 보이도록 클래스 부여
  if (firstVisible) {
    const activeTab = document.querySelector(`.tab-btn[data-tab="${firstVisible}"]`);
    const activePane = document.getElementById(`pane-${firstVisible}`);
    if (activeTab) activeTab.classList.add('active');
    if (activePane) activePane.classList.add('active');
  } else {
    // 생성된 컨텐츠가 없을 경우 기본 탭 유지
    const igTab = document.querySelector('.tab-btn[data-tab="instagram"]');
    const igPane = document.getElementById('pane-instagram');
    if (igTab) igTab.classList.add('active');
    if (igPane) igPane.classList.add('active');
  }
}

function animateGeneratedText(sns, text, imageUrls) {
  const textEl = document.getElementById(`text-${sns}`);
  if (!textEl) return;

  textEl.innerHTML = '';
  
  /* 캐러셀 DOM 주입 */
  const carouselEl = createCarouselElement(sns, imageUrls);
  if (carouselEl) {
    textEl.appendChild(carouselEl);
  }

  const contentWrap = document.createElement('div');
  contentWrap.style.marginTop = '10px';
  textEl.appendChild(contentWrap);

  const cursor = document.createElement('span');
  cursor.className = 'stream-cursor';
  const streamRenderer = uiManager.createStreamRenderer(contentWrap, cursor);

  let index = 0;
  const interval = setInterval(() => {
    if (text[index]) streamRenderer(text[index]);
    index += 1;

    if (index >= text.length) {
      clearInterval(interval);
      uiManager.updatePreview(sns);
      updatePlatformVisibility();
    }
  }, 15);
}

function updatePublishButtonByPlatform(post) {
  const btn = document.getElementById('pmBtnPublish');
  if (!btn || !post) return;

  const snsKey = normalizePlatformKey(post.sns);
  const config = PLATFORM_CONFIG[snsKey];

  btn.dataset.actionType = config?.actionType || 'publish';
  btn.innerHTML = `
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
      <line x1="22" y1="2" x2="11" y2="13" />
      <polygon points="22 2 15 22 11 13 2 9 22 2" />
    </svg>
    ${config?.actionLabel || '즉시 발행'}
  `;
}

/* --------------------------------------------------------------------------
   6. Core Actions
   -------------------------------------------------------------------------- */
   window.generateContent = async function () {
     const requestMeta = buildGenerateRequestParams();
     if (!validateGenerateRequest(requestMeta)) return;

     uiManager.toggleLoading(true);
     state.generatedContent = {};

     try {
       const responseData = await apiService.requestGeneratedContents(requestMeta.payload);

       /* responseData 타입에 따른 분기 처리 (forEach 에러 방지) */
       if (Array.isArray(responseData)) {
         /* 백엔드가 List<SnsResult>를 반환하는 경우 */
         responseData.forEach((res) => {
           const sns = normalizePlatformKey(res.platform);
		   const urls = res.imageUrls || (res.imageUrl ? res.imageUrl.split(',') : state.uploadedImages);
           state.generatedContent[sns] = { 
             text: res.content,
             imageUrl: urls
           };
           animateGeneratedText(sns, res.content, urls);
         });
       } else {
         /* 백엔드가 Map<String, String> 등을 반환하는 경우 */
         requestMeta.activeSNS.forEach((sns) => {
           const text = responseData[sns] || '해당 플랫폼의 생성 결과가 없습니다.';
           state.generatedContent[sns] = { text, imageUrl: state.uploadedImageUrl };
           animateGeneratedText(sns, text, state.uploadedImageUrl);
         });
       }

       uiManager.showToast('AI 콘텐츠 생성이 완료되었습니다.');
     } catch (err) {
       console.error('[App] 콘텐츠 생성 오류:', err);
       uiManager.showToast('콘텐츠 생성 중 오류가 발생했습니다.');
     } finally {
       uiManager.toggleLoading(false);
     }
   };

   window.publishPost = async function (sns) {
     const content = state.generatedContent[sns];

     if (!content?.text) {
       uiManager.showToast('먼저 AI 콘텐츠를 생성해주세요.');
       return;
     }

     /* Rationale: hidden input에 저장된 Pexels URL을 읽어와 API 서비스로 전달 */
     const pexelsUrlInput = document.getElementById('pexels-url-input');
     const pexelsUrl = pexelsUrlInput ? pexelsUrlInput.value : null;

     uiManager.toggleLoading(true);
     uiManager.showToast(`${PLATFORM_CONFIG[sns]?.label || sns}에 게시 중입니다. 잠시만 기다려주세요...`);

     try {
		const externalUrls = state.uploadedImages.filter(url => typeof url === 'string' && !url.startsWith('blob:'));
		const result = await apiService.publishContent(
		  sns,
		  content.text,
		  state.uploadedFiles,
		  externalUrls
		);

       if (result.status === 'success') {
         uiManager.showToast(`${PLATFORM_CONFIG[sns]?.label || sns}에 성공적으로 게시되었습니다!`);
       } else {
         uiManager.showToast(`게시 실패: ${result.message || '알 수 없는 오류'}`);
       }
     } catch (error) {
       console.error('[App] 발행 통신 오류:', error);
       uiManager.showToast('서버와의 통신 중 오류가 발생했습니다.');
     } finally {
       uiManager.toggleLoading(false);
     }
   };
   
   window.copyPostContent = async function (sns) {
     const content = state.generatedContent[sns];

     if (!content?.text) {
       uiManager.showToast('먼저 AI 콘텐츠를 생성해주세요.');
       return;
     }

     try {
       await navigator.clipboard.writeText(content.text);
       uiManager.showToast('복사 완료! 붙여넣어 발행하세요.');
     } catch (error) {
       console.error(error);
       uiManager.showToast('복사 실패');
     }
   };
   
   window.handlePmPublishAction = async function () {
     const modal = document.getElementById('previewModal');
     const sns = normalizePlatformKey(modal?.dataset?.sns || '');

     if (!sns) return;

     const config = PLATFORM_CONFIG[sns];

     if (config?.actionType === 'copy') {
       await copyPostContent(sns);
     } else {
       await publishPost(sns);
     }
   };

/* --------------------------------------------------------------------------
   7. Pending Posts & Data Synchronization
   -------------------------------------------------------------------------- */

/**
 * [공통] DB 또는 생성 API로부터 받은 원본 데이터를 state 규격에 맞게 변환
 * @param {Object} item - 백엔드에서 전달받은 포스트 객체
 */
function mapDtoToPost(item) {
  const snsKey = (item.platform || '').toLowerCase();
  const config = PLATFORM_CONFIG[snsKey] || { label: snsKey, color: '#6366F1', icon: '📌' };
  const palette = PASTEL_PALETTE[snsKey] || {
    backgroundColor: '#f1f5f9',
    textColor: '#475569',
    borderColor: '#cbd5e1'
  };

  return {
    // CRITICAL FIX: 'db-' 접두어를 제거하고 순수 ID(String)만 사용해야 캘린더 드래그 시 DB 저장이 가능함
    id: String(item.id || `pending-${snsKey}-${Date.now()}`), 
    sns: snsKey,
    // 필드명 매핑 (menuName 또는 title 둘 다 대응)
    menu: item.menuName || item.title || '제목 없음',
    bodyText: item.content || '',
    hashtags: item.hashtags ? (Array.isArray(item.hashtags) ? item.hashtags : item.hashtags.split(',').map(t => t.trim())) : [],
    imageUrl: item.imageUrl || null,
    originUrl: item.originUrl || null,
    label: config.label || snsKey.toUpperCase(),
    icon: config.icon || '📄',
    color: config.color || palette.borderColor,
    palette: palette
  };
}

/**
 * 콘텐츠 생성 시 리스트에 추가 (로컬 전용)
 */
function addToPending(sns, menu, bodyText) {
  const newPost = mapDtoToPost({ platform: sns, title: menu, content: bodyText });
  state.pendingPosts.push(newPost);
  renderPendingHTML();
}

/**
 * 대기 목록 UI 렌더링
 */
function renderPendingHTML() {
  const list = document.getElementById('pendingList');
  const badge = document.getElementById('pendingCountBadge');
  const countText = document.getElementById('pending-count-text');

  if (badge) badge.textContent = state.pendingPosts.length;
  if (countText) countText.textContent = `${state.pendingPosts.length} 건의 대기중`;
  if (!list) return;

  if (!state.pendingPosts.length) {
    list.innerHTML = `
      <div class="pending-empty">
        <div class="pe-icon">📭</div>
        <p>콘텐츠를 생성하면<br>여기에 표시됩니다.</p>
      </div>
    `;
    return;
  }

  list.innerHTML = state.pendingPosts
    .map((post) => {
      if (!post) return '';
      const dotColor = post.palette?.borderColor || post.color || '#6366F1';

      return `
        <div class="pending-item" id="${post.id}" draggable="true"
             ondragstart="onPendingDragStart(event,'${post.id}')"
             ondragend="onPendingDragEnd(event)">
          <span class="pi-dot" style="background-color: ${dotColor} !important;"></span>
          <div class="pi-info">
            <div class="pi-name">${post.icon ? post.icon + ' ' : ''}${post.menu}</div>
            <div class="pi-platform">${post.label}</div>
          </div>
          <div class="pi-drag-hint">드래그</div>
          <button class="pi-remove-btn" type="button" onclick="removePending('${post.id}')">✕</button>
        </div>
      `;
    })
    .join('');
}

/**
 * DB로부터 대기 목록 동기화 (수동/자동 통합)
 * @param {boolean} isManual - 수동 클릭 여부 (토스트 메시지 출력 제어)
 */
window.syncPendingPosts = async function(isManual = false) {
  try {
    const response = await fetch('/api/posts/pending');
    if (!response.ok) throw new Error("데이터 로드 실패");
    
    const dbData = await response.json();
    
    // 전체 데이터를 mapDtoToPost를 통해 규격화된 형태로 변환
    state.pendingPosts = dbData.map(item => mapDtoToPost(item));
    
    renderPendingHTML();
    
    if (isManual) {
      uiManager.showToast(`${state.pendingPosts.length}개의 포스트를 불러왔습니다.`);
    }
  } catch (error) {
    console.error("Sync Error:", error);
    if (isManual) uiManager.showToast("데이터를 불러오지 못했습니다.");
  }
};

// 기존 함수명 유지 (버튼 바인딩용)
window.loadPendingFromDB = () => window.syncPendingPosts(true);


 window.loadCenterHistory = async function() {
   uiManager.showToast("최근 기록을 불러오는 중...");
   
   try {
     const response = await fetch('/api/posts/pending');
     if (!response.ok) throw new Error("Network response was not ok");
     
     let data = await response.json();
     if (data.length === 0) return uiManager.showToast("저장된 기록이 없습니다.");

     data.sort((a, b) => b.id - a.id);
     state.generatedContent = {}; 

     data.forEach(item => {
       const sns = (item.platform || '').toLowerCase();
       if (!state.generatedContent[sns]) {
         // 다중 URL 파싱 로직 적용
         const parsedUrls = item.imageUrls ? item.imageUrls : (item.imageUrl ? item.imageUrl.split(',') : []);
         
         state.generatedContent[sns] = {
           text: item.content,
           imageUrls: parsedUrls,
           hashtags: item.hashtags ? (Array.isArray(item.hashtags) ? item.hashtags : item.hashtags.split(',')) : []
         };
         
         const textEl = document.getElementById(`text-${sns}`);
         const resultDiv = document.getElementById(`result-${sns}`);
         const emptyDiv = document.getElementById(`empty-${sns}`);
         
         if (textEl) {
             textEl.innerHTML = '';
             
             // 캐러셀 복원
             const carouselEl = createCarouselElement(sns, parsedUrls);
             if (carouselEl) textEl.appendChild(carouselEl);
             
             // 텍스트 복원
             const textWrap = document.createElement('div');
             textWrap.innerHTML = item.content.replace(/\n/g, '<br>');
             textWrap.style.marginTop = '15px'; 
             textEl.appendChild(textWrap);
         }
         
         if (resultDiv) resultDiv.style.display = 'block';
         if (emptyDiv) emptyDiv.style.display = 'none';
		 if (parsedUrls.length > 0) {
		             state.uploadedImages = [...parsedUrls];
		             updateCenterPreviewsWithImages(parsedUrls);
		             renderImagePreviews(); // 좌측 업로드 미리보기도 동기화
		         }
       }
     });

     updatePlatformVisibility();
     uiManager.showToast("과거 기록이 중앙 영역에 로드되었습니다.");
   } catch (err) {
     console.error(err);
     uiManager.showToast("기록 로드 실패");
   }
 };
window.removePending = function (id) {
  if (typeof CalendarManager !== 'undefined' && CalendarManager.removePending) {
    CalendarManager.removePending(id);
  } else {
    state.pendingPosts = state.pendingPosts.filter(p => p.id !== id);
    renderPendingHTML();
  }
};

/* --------------------------------------------------------------------------
   8. Upload / Retouch & Multi Image Upload
   -------------------------------------------------------------------------- */
let manualUploadedUrls = [];

async function uploadMultipleImagesToServer(files) {
  const formData = new FormData();
  for (let i = 0; i < files.length; i++) {
    formData.append('files', files[i]);
  }
  try {
    const response = await fetch('/api/posts/upload-multiple', {
      method: 'POST',
      body: formData
    });
    if (!response.ok) throw new Error('서버 업로드 실패');
    return await response.json();
  } catch (error) {
    console.error('[Upload Error]:', error);
    throw error;
  }
}

async function handleManualUpload(files) {
  const previewContainer = document.getElementById('imgPreviews');
  if (!previewContainer) return;

  if (files.length > 5) {
    uiManager.showToast('최대 5장까지만 업로드할 수 있습니다.');
    return;
  }
  if (files.length === 0) return;

  previewContainer.innerHTML = '<span style="font-size:13px; color:#64748b;">이미지 업로드 중...</span>';

  try {
    uiManager.toggleLoading(true);
    const uploadResult = await uploadMultipleImagesToServer(files);

    if (uploadResult.status === 'success' && uploadResult.urls) {
		
		manualUploadedUrls.push(...uploadResult.urls);
		state.uploadedImages.push(...uploadResult.urls);
		state.uploadedFiles.push(...Array.from(files));

		state.uploadedImageUrl = state.uploadedImages[0]; 
		renderImagePreviews();
      updateRetouchState();
      uiManager.showToast('이미지가 성공적으로 업로드되었습니다.');
    }
  } catch (e) {
    previewContainer.innerHTML = '<span style="font-size:13px; color:#ef4444;">업로드에 실패했습니다. 다시 시도해주세요.</span>';
  } finally {
    uiManager.toggleLoading(false);
  }
}

function handleFiles(files) {
  handleManualUpload(files);
}

function renderImagePreviews() {
  const previewContainer = document.getElementById('imgPreviews');
  if (!previewContainer) return;

  previewContainer.style.display = 'flex';
  previewContainer.style.gap = '10px';
  previewContainer.style.flexWrap = 'wrap';
  previewContainer.style.marginTop = '12px';

  previewContainer.innerHTML = state.uploadedImages
    .map(
      (src, i) => `
        <div class="img-thumb-wrap" style="position:relative;">
          <img class="img-thumb" src="${src}" alt="" style="width:80px; height:80px; object-fit:cover; border-radius:8px; border:1px solid #e2e8f0;">
          <button class="remove-btn" type="button" onclick="removeImg(${i})" style="position:absolute; top:-5px; right:-5px; background:#ef4444; color:#fff; border:none; border-radius:50%; width:20px; height:20px; cursor:pointer; font-size:12px; line-height:1;">×</button>
        </div>
      `
    )
    .join('');
}

function updateRetouchState() {
  const activeSNS = getActiveSNS();
  const enabled =
    (activeSNS.includes('instagram') || activeSNS.includes('facebook')) &&
    state.uploadedImages.length > 0;

  const btn = document.getElementById('retouchBtn');
  if (btn) btn.disabled = !enabled;
}

window.removeImg = function (index) {
  const removedUrl = state.uploadedImages[index];
  
  /* File 객체와 1:1 매칭되는 로컬 업로드 URL인지 식별하여 분기 삭제 */
  const manualIndex = manualUploadedUrls.indexOf(removedUrl);
  if (manualIndex > -1) {
    manualUploadedUrls.splice(manualIndex, 1);
    state.uploadedFiles.splice(manualIndex, 1);
  }

  state.uploadedImages.splice(index, 1);
  state.uploadedImageUrl = state.uploadedImages.length > 0 ? state.uploadedImages[0] : null;

  renderImagePreviews();
  updateRetouchState();
};

window.openRetouchModal = function () {
  document.getElementById('retouchModal')?.classList.add('open');
};

window.closeRetouchModal = function () {
  document.getElementById('retouchModal')?.classList.remove('open');
};

window.applyRetouchAndClose = function () {
  window.closeRetouchModal();
};

/* --------------------------------------------------------------------------
   9. UI Bindings
   -------------------------------------------------------------------------- */
const rangeEl = document.getElementById('lengthRange');
const rangeValEl = document.getElementById('rangeVal');
const menuInput = document.getElementById('menuName');
const menuCount = document.getElementById('menuCount');
const uploadZone = document.getElementById('uploadZone');
const imgInput = document.getElementById('imgInput');

function updateRange() {
  if (!rangeEl || !rangeValEl) return;
  const pct = ((rangeEl.value - rangeEl.min) / (rangeEl.max - rangeEl.min)) * 100;
  rangeEl.style.setProperty('--pct', `${pct}%`);
  rangeValEl.textContent = `${rangeEl.value}자`;
}

function bindFormEvents() {
  const form = document.getElementById('contentForm');
  form?.addEventListener('submit', (e) => e.preventDefault());
}

function bindRangeEvents() {
  if (!rangeEl) return;
  rangeEl.addEventListener('input', updateRange);
  updateRange();
}

function bindMenuCounterEvents() {
  if (!menuInput || !menuCount) return;

  menuInput.addEventListener('input', () => {
    const len = menuInput.value.length;
    menuCount.textContent = `${len}/50`;
    menuCount.classList.toggle('warn', len > 40);
  });
}

function bindToneEvents() {
  document.querySelectorAll('#toneGroup .tone-item').forEach((item) => {
    item.addEventListener('click', () => {
      item.classList.toggle('active');

      if (!document.querySelectorAll('#toneGroup .tone-item.active').length) {
        document
          .querySelector('#toneGroup .tone-item[data-tone="default"]')
          ?.classList.add('active');
      }
    });
  });
}

function bindEmojiEvents() {
  document.querySelectorAll('#emojiGroup .slider-item').forEach((item) => {
    item.addEventListener('click', () => {
      document
        .querySelectorAll('#emojiGroup .slider-item')
        .forEach((node) => node.classList.remove('active'));
      item.classList.add('active');
    });
  });
}

function bindTabEvents() {
  const tabButtons = document.querySelectorAll('.tab-btn');
  const panes = document.querySelectorAll('.output-pane');

  tabButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      tabButtons.forEach((b) => b.classList.remove('active'));
      panes.forEach((pane) => pane.classList.remove('active'));

      btn.classList.add('active');
      document.getElementById(`pane-${btn.dataset.tab}`)?.classList.add('active');
    });
  });
}

function bindSnsChipEvents() {
  document.querySelectorAll('#snsChips .chip').forEach((chip) => {
    chip.addEventListener('click', () => {
      chip.classList.toggle('active');

      if (chip.id === 'communityChip') {
        const isActive = chip.classList.contains('active');
        chip.style.background = isActive ? '#6366F1' : '';
        chip.style.color = isActive ? '#fff' : '#6366F1';
      }

      updateRetouchState();
    });
  });
}

function bindUploadEvents() {
  uploadZone?.addEventListener('click', () => imgInput?.click());

  uploadZone?.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadZone.style.borderColor = 'rgba(20,184,166,0.35)';
  });

  uploadZone?.addEventListener('dragleave', () => {
    uploadZone.style.borderColor = '';
  });

  uploadZone?.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadZone.style.borderColor = '';
    if (e.dataTransfer.files) handleFiles(e.dataTransfer.files);
  });

  imgInput?.addEventListener('change', () => handleFiles(imgInput.files));
}

function bindAllUIEvents() {
  bindFormEvents();
  bindRangeEvents();
  bindMenuCounterEvents();
  bindToneEvents();
  bindEmojiEvents();
  bindTabEvents();
  bindSnsChipEvents();
  bindUploadEvents();
}

/* --------------------------------------------------------------------------
   10. Dashboard Init
   -------------------------------------------------------------------------- */
async function initializeDashboard() {
  if (typeof CalendarManager !== 'undefined') {
    try {
      CalendarManager.init({
        state,
        uiManager,
        PLATFORM_CONFIG,
        PASTEL_PALETTE,
        renderPendingHTML,
        publishPost: window.publishPost
      });
      CalendarManager.initCalendar();
      console.log("[Dash] Calendar initialized");
    } catch (e) {
      console.error("[Dash] Calendar Init Error:", e);
    }
  }
  updateRetouchState();
}

// ── 마이페이지 기본값 pre-select ──────────────────────────
function applyContentDefaults() {
  const tone  = document.getElementById('cs_tone')?.value;
  const emoji = document.getElementById('cs_emoji')?.value;
  const len   = document.getElementById('cs_length')?.value;
  const sns   = document.getElementById('cs_sns')?.value;

  // hidden input 없으면(useDefaultMode=OFF 또는 비로그인) 적용 안 함
  if (!tone && !emoji && !len && !sns) return;

  // 말투: DB값(기본/친근/깔끔/격식/트렌디) → data-tone(default/friendly/clean/formal/trendy)
  const TONE_MAP = { '기본':'default', '친근':'friendly', '깔끔':'clean', '격식':'formal', '트렌디':'trendy' };
  if (tone && TONE_MAP[tone]) {
    document.querySelectorAll('#toneGroup .tone-item').forEach(el => {
      el.classList.toggle('active', el.dataset.tone === TONE_MAP[tone]);
    });
  }

  // 이모지: DB값(적게/적당히/많이) → data-emoji(low/mid/high)
  const EMOJI_MAP = { '적게':'low', '적당히':'mid', '많이':'high' };
  if (emoji && EMOJI_MAP[emoji]) {
    document.querySelectorAll('#emojiGroup .slider-item').forEach(el => {
      el.classList.toggle('active', el.dataset.emoji === EMOJI_MAP[emoji]);
    });
  }

  // 글자수 슬라이더
  if (len && rangeEl && rangeValEl) {
    rangeEl.value = len;
    rangeValEl.textContent = len + '자';
    updateRange(); // 슬라이더 스타일(--pct) 갱신
  }

  // SNS 칩: "instagram,naver" → 각 chip active 토글
  if (sns) {
    const selected = sns.split(',').map(s => s.trim());
    document.querySelectorAll('#snsChips .chip').forEach(el => {
      el.classList.toggle('active', selected.includes(el.dataset.sns));
    });
  }
}

/* --------------------------------------------------------------------------
   11. Boot
   -------------------------------------------------------------------------- */
   
document.addEventListener('DOMContentLoaded', async () => {
  bindAllUIEvents();
  await initializeDashboard();
});


/* --------------------------------------------------------------------------
   Content Init (DB Integration)
   -------------------------------------------------------------------------- */
async function loadInitialContentSettings() {
    // Thymeleaf hidden input으로부터 업종 코드 추출
    const industryCode = document.getElementById('businessCategory')?.value || 'DEFAULT';
    // 유저 식별자 (실제 환경에서는 세션 또는 시큐리티 컨텍스트 사용)
    const userId = 1; 

    try {
        const response = await fetch(`/api/posts/init?industryCode=${industryCode}&userId=${userId}`);
        if (!response.ok) throw new Error('초기 데이터 로드에 실패했습니다.');
        
        const data = await response.json();
        
        // 1. 업종별 키워드 동적 렌더링
        renderKeywords(data.keywords);
        
        // 2. 플랫폼별 SNS 가이드 업데이트
        updateSnsGuides(data.snsGuides);
        
        // 3. 사용자 환경 설정 적용
        applyUserSettings(data.userSetting);

    } catch (error) {
        console.error('[Init Error]:', error);
    }
}


function updateSnsGuides(guides) {
    const platformMap = {
        instagram: { selector: '.guide-box.ig', name: 'Instagram', color: 'rgba(225,48,108,0.28)' },
        facebook: { selector: '.guide-box.fb', name: 'Facebook', color: 'rgba(24,119,242,0.28)' },
        naver: { selector: '.guide-box.nv', name: '네이버', color: 'rgba(3,199,90,0.28)' },
        kakao: { selector: '.guide-box.kk', name: '카카오', color: 'rgba(254,229,0,0.24)' },
        community: { selector: '.guide-box.comm', name: '커뮤니티', color: 'rgba(99,102,241,0.28)' }
    };

    guides.forEach(guide => {
        const config = platformMap[guide.platform.toLowerCase()];
        if (!config) return;

        const container = document.querySelector(config.selector);
        if (container) {
            // 하드코딩된 HTML을 제거하고 DB 데이터를 바탕으로 가이드 박스를 완전히 재구성합니다.
            container.innerHTML = `
                <strong>📌 ${config.name} 가이드</strong><br>
                ${guide.guideContent}<br>
                <span class="time-tip" style="margin-top:7px;display:inline-flex;border-color:${config.color};">
                    ⏰ 추천 시간: <b style="color:var(--text);margin-left:4px;">${guide.bestTime || '--:--'}</b>
                </span>
            `;
        }
    });
}

function applyUserSettings(settings) {
    // 게시 SNS 활성화 상태 반영
    const chips = document.querySelectorAll('#snsChips .chip');
    chips.forEach(chip => {
        const sns = chip.getAttribute('data-sns');
        if (settings.activeSns.includes(sns)) {
            chip.classList.add('active');
        } else {
            chip.classList.remove('active');
        }
    });

    // 말투 스타일 반영
    const toneItems = document.querySelectorAll('#toneGroup .tone-item');
    toneItems.forEach(item => {
        item.classList.toggle('active', item.getAttribute('data-tone') === settings.toneStyle);
    });

    // 이모지 사용량 반영
    const emojiItems = document.querySelectorAll('#emojiGroup .slider-item');
    emojiItems.forEach(item => {
        item.classList.toggle('active', item.getAttribute('data-emoji') === settings.emojiLevel);
    });

    // 글자 수 제한 반영
    const lengthRange = document.getElementById('lengthRange');
    const rangeVal = document.getElementById('rangeVal');
    if (lengthRange && rangeVal) {
        lengthRange.value = settings.maxLength;
        rangeVal.textContent = `${settings.maxLength}자`;
    }

    // 전역 state 객체 동기화 (콘텐츠 생성 시 API 파라미터로 활용)
    if (typeof state !== 'undefined') {
        state.platforms = settings.activeSns;
        state.tones = settings.toneStyle;
        state.emojiLevel = settings.emojiLevel;
        state.maxLength = settings.maxLength;
    }
}

// DOM 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
    loadInitialContentSettings();
});


/* Global variables for Pexels state */
let pexelsCurrentPage = 1;
let pexelsCurrentQuery = '';

/* Modal Control Functions */
function openPexelsModal() {
    document.getElementById('pexelsModal').style.display = 'flex';
}

function closePexelsModal() {
    document.getElementById('pexelsModal').style.display = 'none';
}

/* Close modal on outside click */
window.addEventListener('click', function(e) {
    const pexelsModal = document.getElementById('pexelsModal');
    if (e.target === pexelsModal) {
        closePexelsModal();
    }
});

/* Search execution */
/* Rationale: 검색 API 호출 완료 시 renderPexelsResults를 호출하여 단일 선택 로직으로 파이프라인을 연결 */
function executePexelsSearch(isAppend) {
    const inputPexelsQuery = document.getElementById('pexelsQuery');
    const pexelsGallery = document.getElementById('pexelsGallery');
    const btnLoadMorePexels = document.getElementById('btnLoadMorePexels');
    const modalBodyScroll = document.getElementById('modalBodyScroll');

    if (!isAppend) {
        const query = inputPexelsQuery.value.trim();
        if (!query) return;
        pexelsCurrentQuery = query;
        pexelsCurrentPage = 1;
        pexelsGallery.innerHTML = '<p style="grid-column: 1 / -1; text-align:center; color:#94a3b8; font-size:14px; margin-top:20px;">검색 중...</p>';
        btnLoadMorePexels.style.display = 'none';
        modalBodyScroll.scrollTop = 0;
    }

    fetch(`/api/sns/pexels/search?query=${encodeURIComponent(pexelsCurrentQuery)}&page=${pexelsCurrentPage}`)
        .then(response => {
            if (!response.ok) throw new Error('API fetch failed');
            return response.json();
        })
        .then(data => {
            if (!isAppend) pexelsGallery.innerHTML = '';

            if (!data.photos || data.photos.length === 0) {
                if (!isAppend) pexelsGallery.innerHTML = '<p style="grid-column: 1 / -1; text-align:center; font-size:14px; color:#64748b;">결과가 없습니다.</p>';
                btnLoadMorePexels.style.display = 'none';
                return;
            }

            /* Fix: 올바른 렌더링 함수 호출 */
            renderPexelsResults(data.photos);
            btnLoadMorePexels.style.display = 'block';
        })
        .catch(error => {
            if (!isAppend) pexelsGallery.innerHTML = '<p style="color:#ef4444; grid-column: 1 / -1; text-align:center; font-size:14px;">오류가 발생했습니다.</p>';
            console.error(error);
        });
}

function selectPexelsImage(imageUrl) {
    const hiddenInput = document.getElementById('pexels-url-input');
    const localFileInput = document.getElementById('imgInput');
    const previewContainer = document.getElementById('imgPreviews');

    if (hiddenInput) {
        hiddenInput.value = imageUrl;
    }

    /* Rationale: 로컬 파일 입력 폼 초기화 및 전역 state 객체 동기화. 
       이를 통해 생성 버튼 클릭 시 state.uploadedImageUrl 값을 읽어 서버로 정상 전송함. */
    if (localFileInput) localFileInput.value = '';
    
    state.uploadedImageUrl = imageUrl;
    state.uploadedImages = [imageUrl];
    state.uploadedFiles = [];
    updateRetouchState(); 

    previewContainer.innerHTML = ''; 

    const previewWrapper = document.createElement('div');
    previewWrapper.style.position = 'relative';
    previewWrapper.style.display = 'inline-block';
    previewWrapper.style.width = '100px'; 
    previewWrapper.style.height = '100px';
    previewWrapper.style.marginRight = '10px';

    const imgElement = document.createElement('img');
    imgElement.src = imageUrl;
    imgElement.style.width = '100%';
    imgElement.style.height = '100%';
    imgElement.style.objectFit = 'cover';
    imgElement.style.borderRadius = '8px';

    const removeBtn = document.createElement('button');
    removeBtn.innerText = 'X';
    removeBtn.style.position = 'absolute';
    removeBtn.style.top = '4px';
    removeBtn.style.right = '4px';
    removeBtn.style.background = 'rgba(0,0,0,0.5)';
    removeBtn.style.color = '#fff';
    removeBtn.style.border = 'none';
    removeBtn.style.borderRadius = '50%';
    removeBtn.style.width = '20px';
    removeBtn.style.height = '20px';
    removeBtn.style.fontSize = '10px';
    removeBtn.style.cursor = 'pointer';

    removeBtn.onclick = function() {
        if (hiddenInput) hiddenInput.value = '';
        state.uploadedImageUrl = null;
        state.uploadedImages = [];
        previewContainer.innerHTML = '';
        updateRetouchState();
    };

    previewWrapper.appendChild(imgElement);
    previewWrapper.appendChild(removeBtn);
    previewContainer.appendChild(previewWrapper);

    closePexelsModal();
}

function renderPexelsData(photos) {
    const pexelsGallery = document.getElementById('pexelsGallery');
    
    photos.forEach(photo => {
        const img = document.createElement('img');
        img.src = photo.src.medium;
        
        img.onclick = function() {
            appendImageToPreview(photo.src.large);
            closePexelsModal();
        };
        
        pexelsGallery.appendChild(img);
    });
}

function appendImageToPreview(imageUrl) {
    const imgPreviewsContainer = document.getElementById('imgPreviews');
    
    const wrapper = document.createElement('div');
    wrapper.style.position = 'relative';
    wrapper.style.display = 'inline-block';
    wrapper.style.margin = '4px';

    const imgElement = document.createElement('img');
    imgElement.src = imageUrl;
    imgElement.style.width = '70px';
    imgElement.style.height = '70px';
    imgElement.style.objectFit = 'cover';
    imgElement.style.borderRadius = '8px';
    imgElement.style.border = '1px solid #e2e8f0';

    const delBtn = document.createElement('button');
    delBtn.innerHTML = '×';
    delBtn.style.position = 'absolute';
    delBtn.style.top = '-5px';
    delBtn.style.right = '-5px';
    delBtn.style.background = '#ef4444';
    delBtn.style.color = '#fff';
    delBtn.style.border = 'none';
    delBtn.style.borderRadius = '50%';
    delBtn.style.width = '18px';
    delBtn.style.height = '18px';
    delBtn.style.fontSize = '12px';
    delBtn.style.lineHeight = '1';
    delBtn.style.cursor = 'pointer';
    
    delBtn.onclick = function() {
        wrapper.remove();
        removeUrlFromHiddenInput(imageUrl);
    };

    wrapper.appendChild(imgElement);
    wrapper.appendChild(delBtn);
    imgPreviewsContainer.appendChild(wrapper);
    
    addUrlToHiddenInput(imageUrl);
}

/* Logic for maintaining image URLs for Spring Boot submission */
function addUrlToHiddenInput(url) {
    let hiddenInput = document.getElementById('pexelsSelectedUrls');
    if (!hiddenInput) {
        hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.id = 'pexelsSelectedUrls';
        hiddenInput.name = 'pexelsImageUrls';
        document.getElementById('uploadZone').parentNode.appendChild(hiddenInput);
    }
    
    let currentUrls = hiddenInput.value ? hiddenInput.value.split(',') : [];
    currentUrls.push(url);
    hiddenInput.value = currentUrls.join(',');
}

function removeUrlFromHiddenInput(url) {
    const hiddenInput = document.getElementById('pexelsSelectedUrls');
    if (hiddenInput) {
        let currentUrls = hiddenInput.value.split(',');
        currentUrls = currentUrls.filter(u => u !== url);
        hiddenInput.value = currentUrls.join(',');
    }
}

function updateCenterPreviewsWithImages(imageUrls) {
    let urls = [];
    if (Array.isArray(imageUrls)) urls = imageUrls;
    else if (typeof imageUrls === 'string') urls = imageUrls.split(',');
    
    urls = urls.filter(Boolean);

    if (urls.length === 0) {
        clearCenterPreviews();
        return;
    }

    // --- Instagram 목업 업데이트 ---
    const igPlaceholder = document.getElementById('igPhotoPlaceholder');
    const igImg = document.getElementById('igPhotoImg');
    
    if (igPlaceholder) igPlaceholder.style.display = 'none';
    if (igImg) igImg.style.display = 'none'; // 기존 단일 img 태그는 숨김

    const igContainer = igImg ? igImg.parentElement : null;
    if (igContainer) {
        // 기존에 생성해둔 캐러셀이 있다면 초기화
        const existing = igContainer.querySelector('.sns-carousel-wrapper');
        if (existing) existing.remove();

        // 새 캐러셀 주입
        const carousel = createCarouselElement('center-ig', urls);
        if (carousel) igContainer.appendChild(carousel);
    }

    // --- Facebook / Kakao 목업 업데이트 ---
    const fbWrap = document.getElementById('fbsMediaWrap');
    if (fbWrap) {
        fbWrap.innerHTML = '';
        const fbCarousel = createCarouselElement('center-fb', urls);
        if (fbCarousel) fbWrap.appendChild(fbCarousel);
    }

    const kkWrap = document.getElementById('kksMediaSection');
    if (kkWrap) {
        kkWrap.innerHTML = '';
        const kkCarousel = createCarouselElement('center-kk', urls);
        if (kkCarousel) kkWrap.appendChild(kkCarousel);
    }
}

function clearCenterPreviews() {
    const igImg = document.getElementById('igPhotoImg');
    const igPlaceholder = document.getElementById('igPhotoPlaceholder');
    if(igImg && igPlaceholder) { 
        igImg.style.display = 'none'; 
        igImg.src = ''; 
        igPlaceholder.style.display = 'flex'; 
    }

    const blogImg = document.getElementById('blogArtImg');
    const blogPlaceholder = document.getElementById('blogArtImgPlaceholder');
    if(blogImg && blogPlaceholder) { 
        blogImg.style.display = 'none'; 
        blogImg.src = ''; 
        blogPlaceholder.style.display = 'flex'; 
    }

    const fbWrap = document.getElementById('fbsMediaWrap');
    if(fbWrap) fbWrap.innerHTML = '';

    const kkWrap = document.getElementById('kksMediaSection');
    if(kkWrap) kkWrap.innerHTML = '';
}

/* Event binding for Search Button and Enter Key */
document.addEventListener('DOMContentLoaded', function() {
    const btnSearchPexels = document.getElementById('btnSearchPexels');
    const inputPexelsQuery = document.getElementById('pexelsQuery');
    const btnLoadMorePexels = document.getElementById('btnLoadMorePexels');

    if(btnSearchPexels) {
        btnSearchPexels.onclick = function() { executePexelsSearch(false); };
    }
    
    if(inputPexelsQuery) {
        inputPexelsQuery.onkeypress = function(e) {
            if (e.key === 'Enter') {
                executePexelsSearch(false);
            }
        };
    }
    
    if(btnLoadMorePexels) {
        btnLoadMorePexels.onclick = function() {
            pexelsCurrentPage++;
            const originalText = this.innerText;
            this.innerText = '불러오는 중...';
            executePexelsSearch(true);
            setTimeout(() => { this.innerText = originalText; }, 500);
        };
    }
});









document.addEventListener("DOMContentLoaded", function() {
    /* Initialize tab and pane visibility based on requirement */
    const defaultPlatform = "instagram"; 
    
    /* Activate default tab button */
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        if(btn.getAttribute('data-tab') === defaultPlatform) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    /* Hide all panes initially */
    const panes = document.querySelectorAll('.output-pane');
    panes.forEach(pane => {
        pane.style.display = 'none'; 
    });
    
    /* Activate default output pane */
    const activePane = document.getElementById('pane-' + defaultPlatform);
    if(activePane) {
        activePane.style.display = 'block';
        
        /* Ensure empty state is visible and result state is hidden */
        const emptyState = activePane.querySelector('.empty-state');
        const resultState = activePane.querySelector('#result-' + defaultPlatform);
        
        if(emptyState) emptyState.style.display = 'block';
        if(resultState) resultState.style.display = 'none';
    }

    /* Bind click events for all tab buttons */
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');
            
            /* Remove active class from all tabs and hide all panes */
            tabButtons.forEach(btn => btn.classList.remove('active'));
            panes.forEach(pane => pane.style.display = 'none');
            
            /* Add active class to the clicked tab and show its pane */
            this.classList.add('active');
            const targetPane = document.getElementById('pane-' + targetTab);
            if(targetPane) {
                targetPane.style.display = 'block';
            }
        });
    });
});








function selectPexelsImage(imageUrl) {
    const hiddenInput = document.getElementById('pexels-url-input');
    
    if (hiddenInput) {
        const currentUrls = hiddenInput.value ? hiddenInput.value.split(',') : [];
        currentUrls.push(imageUrl);
        hiddenInput.value = currentUrls.join(',');
    }

    if (typeof state !== 'undefined') {
        state.uploadedImages.push(imageUrl);
        state.uploadedImageUrl = state.uploadedImages[0];
    }

    /* DOM 직접 조작 대신 기존에 정의된 통합 렌더링 함수 재사용 */
    renderImagePreviews();

    if (typeof updateCenterPreviewsWithImageUrl === 'function') {
        updateCenterPreviewsWithImages(state.uploadedImages);
    }
    if (typeof updateRetouchState === 'function') {
        updateRetouchState();
    }
    
    const modal = document.getElementById('pexelsModal');
    if (modal) modal.style.display = 'none';
}


/* * Example of how the dynamically created Pexels image results 
 * should be bound to the selection function.
 */
function renderPexelsResults(images) {
    const gallery = document.getElementById('pexelsGallery');
    gallery.innerHTML = '';

    images.forEach(image => {
        const img = document.createElement('img');
        img.src = image.src.medium;
        
        /* Bind the click event to trigger the preview logic */
        img.onclick = function() {
            selectPexelsImage(image.src.large2x);
        };
        
        gallery.appendChild(img);
    });
}

   document.addEventListener('DOMContentLoaded', async () => {
     bindAllUIEvents();
     applyContentDefaults(); // ✅ 추가 — bindAllUIEvents 이후 실행해야 이벤트 바인딩과 충돌 없음
     await initializeDashboard();
   });

   
   document.addEventListener("DOMContentLoaded", async function() {
       // 1. 숨겨둔 내 브랜드 업종 코드를 가져옵니다. (예: 'CAFE')
       const hiddenInput = document.getElementById("myIndustryCode");
       const myIndustryCode = hiddenInput ? hiddenInput.value : null;

       // 업종 코드가 존재하면 바로 키워드를 불러옵니다.
       if (myIndustryCode) {
           await fetchAndRenderKeywords(myIndustryCode);
       }

       async function fetchAndRenderKeywords(industryCode) {
           const keywordGroup = document.getElementById('keywordGroup');
           if (!keywordGroup) return;

           try {
               // 로딩 상태 UI (기존 Fallback UI 스타일 적용)
               keywordGroup.innerHTML = `
                   <div style="width:100%; padding:10px; font-size:12px; color:#64748b; text-align:center;">
                       키워드 데이터를 불러오는 중...
                   </div>
               `;

               // 백엔드 API 호출
               const response = await fetch(`/api/keywords?industryCode=${industryCode}`);
               if (!response.ok) throw new Error("네트워크 응답 에러");
               
               const keywords = await response.json();

               // 2. 데이터가 없거나 배열이 비어있는 경우 (작성하신 Fallback UI)
               if (!keywords || keywords.length === 0) {
                   keywordGroup.innerHTML = `
                       <div style="width:100%; padding:10px; font-size:12px; color:#64748b; text-align:center;">
                           현재 업종에 등록된 추천 키워드가 없습니다.
                       </div>
                   `;
                   return;
               }

               // 3. 정상적으로 데이터가 있을 경우 동적 렌더링 (작성하신 HTML 구조 적용)
               // 💡 주의: 체크박스 폼 전송을 위해 name="keywords" 속성을 추가했습니다.
               keywordGroup.innerHTML = keywords.map((kw, index) => `
                   <div class="keyword-item">
                       <input type="checkbox" name="keywords" id="kw-${index}" value="${kw.name}">
                       <label class="keyword-label" for="kw-${index}">${kw.name}</label>
                   </div>
               `).join('');

           } catch (error) {
               console.error("키워드 로드 실패:", error);
               // 에러 발생 시 UI
               keywordGroup.innerHTML = `
                   <div style="width:100%; padding:10px; font-size:12px; color:red; text-align:center;">
                       키워드를 불러오는데 실패했습니다.
                   </div>
               `;
           }
       }
   });
