/* ==========================================================================
   소셜다모아 통합 Frontend Controller (app.js + app2.js)
   ========================================================================== */

/* --------------------------------------------------------------------------
   1. Config & Mock Data
   -------------------------------------------------------------------------- */
const PLATFORM_CONFIG = {
  instagram: { label: 'Instagram', color: '#E1306C', icon: '📷' },
  facebook:  { label: 'Facebook', color: '#1877F2', icon: '📘' },
  naver:     { label: '네이버 블로그', color: '#03C75A', icon: '🟢' },
  kakao:     { label: '카카오 채널', color: '#FEE500', icon: '💛' },
  community: { label: '커뮤니티', color: '#6366F1', icon: '💬' }
};

const MOCK_DATA = {
  comments: [], 
  calendarEvents: [], 
  communities: {} 
};


/* --------------------------------------------------------------------------
   2. State Management (상태 관리)
   -------------------------------------------------------------------------- */
const state = {
  uploadedImages: [],
  uploadedFiles: [],
  communityTags: [],
  currentFilter: 'none',
  appliedFilter: 'none',
  generatedContent: {}, 
  scheduledEvents: [],  
  pendingPosts: [],     
  calendarInstance: null
};

/* --------------------------------------------------------------------------
   3. API Service Layer (Spring Boot 백엔드 연동부)
   -------------------------------------------------------------------------- */
const apiService = {
  async generateContent(params, onStreamChunk) {
    try {
      console.log('서버로 전송될 AI 생성 파라미터:', params);

      // 🚨 기존에 있던 mockAIStreamCall을 지우고, 여기서 바로 Spring Boot로 API 요청을 보냅니다.
      // (엔드포인트 주소는 실제 스프링 부트 컨트롤러에 맞게 수정해 주세요)
      const response = await fetch('/api/v1/content/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          menuName: params.menuName,
          extraInfo: params.extraInfo,
          keywords: params.keywords,
          tones: params.tones,
          platform: params.platform
        })
      });

      if (!response.ok) {
        throw new Error(`서버 통신 오류: ${response.status}`);
      }

      // JSON 응답 파싱 (스프링 부트에서 넘겨주는 필드명에 맞게 data.text를 수정하세요)
      const data = await response.json();
      const generatedText = data.text || "응답 텍스트를 찾을 수 없습니다."; 

      // 프론트엔드에서 한 글자씩 타이핑되는 애니메이션 효과 처리
      return new Promise((resolve) => {
        let i = 0;
        const interval = setInterval(() => {
          if (onStreamChunk && generatedText[i]) {
            onStreamChunk(generatedText[i]);
          }
          i++;
          if (i >= generatedText.length) {
            clearInterval(interval);
            resolve({ text: generatedText });
          }
        }, 15); // 타이핑 속도
      });

    } catch (error) {
      console.error('AI 생성 API 오류:', error);
      throw error;
    }
  },

  // 아래 두 함수는 그대로 유지합니다.
  async fetchDashboardData() {
    return { events: [], pending: [] };
  },

  async publishToChannel(snsType, contentData) {
    console.log(`[${snsType}] 발행 요청:`, contentData);
	updatePlatformVisibility();
    return { success: true };
  }
};

/* --------------------------------------------------------------------------
   4. UI Manager (화면 렌더링 제어)
   -------------------------------------------------------------------------- */
const uiManager = {
  showToast(msg) { 
    // TODO: 추후 멋진 Toast UI로 교체 가능
    alert(msg); 
  },
  
  toggleLoading(isLoading) {
    const btn = document.getElementById('generateBtn');
    const progress = document.getElementById('genProgressBar');
    if (!btn) return;
    
    if (isLoading) {
      btn.classList.add('loading');
      if (progress) progress.classList.add('active');
    } else {
      btn.classList.remove('loading');
      if (progress) progress.classList.remove('active');
    }
  },

  renderPendingList() { 
    if (typeof renderPendingHTML === 'function') renderPendingHTML(); 
  },
  
  updatePreview(sns, data) { 
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
  }
};

/* --------------------------------------------------------------------------
   5. Core Logic & Event Controllers
   -------------------------------------------------------------------------- */
window.generateContent = async function() {
  const menuEl = document.getElementById('menuName');
  const extraEl = document.getElementById('extraInfo');
  
  const menu = menuEl ? menuEl.value.trim() : '';
  const extra = extraEl ? extraEl.value.trim() : '';
  const keywords = getSelectedKeywords();
  const activeSNS = [...document.querySelectorAll('#snsChips .chip.active')].map(c => c.dataset.sns);

  // 유효성 검사
  if (!menu) return uiManager.showToast('필수 값(메뉴 / 상품명)을 입력해주세요.');
  if (keywords.length === 0) return uiManager.showToast('추가 정보 키워드를 1개 이상 선택해주세요.');
  if (activeSNS.length === 0) return uiManager.showToast('게시할 SNS를 1개 이상 선택해 주세요.');


  // ContentRequest.java DTO와 이름, 타입을 일치해야함
    const requestParams = {
      menuName: menu,                     // String
      extraInfo: extra,                   // String
      keywords: keywords,                 // List<String> 이므로 자바스크립트 배열 그대로 전송!
      platforms: activeSNS.join(','),     // String 이므로 쉼표로 연결해서 전송! ("instagram,naver")
      tones: getActiveTones().join(','),  // String 이므로 쉼표로 연결해서 전송! ("friendly,trendy")
      emojiLevel: getActiveEmojiLevel(),  // String
      maxLength: lengthRange ? parseInt(lengthRange.value, 10) : 300 // maxLen -> maxLength 이름 수정!
    };

  uiManager.toggleLoading(true);
  state.generatedContent = {};

  try {
    // 💡 여러 번 쏘지 않고, 서버에 데이터를 딱 1번만 요청합니다!
    const response = await fetch('/api/v1/content/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestParams)
    });

    if (!response.ok) throw new Error(`서버 오류: ${response.status}`);
    
    // 백엔드에서 온 데이터: { "instagram": "...", "naver": "..." }
    const responseData = await response.json(); 

    // 선택했던 SNS 탭들에 순회하며 애니메이션과 함께 결과물 분배
    activeSNS.forEach(sns => {
      const textEl = document.getElementById(`text-${sns}`);
      if (!textEl) return;

      // 맵핑된 결과가 없으면 임시 메시지 출력
      const generatedText = responseData[sns] || "해당 플랫폼의 생성 결과가 없습니다.";
      state.generatedContent[sns] = { text: generatedText };

      // 타이핑 애니메이션 적용
      const cursor = document.createElement('span');
      cursor.className = 'stream-cursor';
      const streamRenderer = uiManager.createStreamRenderer(textEl, cursor);

      let i = 0;
      const interval = setInterval(() => {
        if (generatedText[i]) streamRenderer(generatedText[i]);
        i++;
        if (i >= generatedText.length) {
          clearInterval(interval);
          uiManager.updatePreview(sns, state.generatedContent[sns]);
          addToPending(sns, menu, generatedText); // 스케줄러 등록
        }
      }, 15);
    });

    uiManager.showToast('✨ AI 콘텐츠 생성이 완료되었습니다.');

  } catch (err) {
    console.error('LLM API 호출 오류:', err);
    uiManager.showToast('콘텐츠 생성 중 오류가 발생했습니다.');
  } finally {
    uiManager.toggleLoading(false);
  }
};


   /* --------------------------------------------------------------------------
      실제 API 게시하기 (Instagram, Facebook 등)
      -------------------------------------------------------------------------- */
	  window.publishPost = async function(sns) {
	    const content = state.generatedContent[sns];
	    if (!content || !content.text) return uiManager.showToast('먼저 AI 콘텐츠를 생성해주세요.');

	    uiManager.toggleLoading(true);
	    uiManager.showToast(`⏳ ${PLATFORM_CONFIG[sns].label}에 게시 중입니다. 잠시만 기다려주세요...`);

	    try {
	      // 💡 JSON 방식 대신 FormData 객체 생성
	      const formData = new FormData();
	      formData.append('platform', sns);
	      formData.append('text', content.text);

	      // 사용자가 업로드한 이미지가 있다면 첨부 (첫 번째 이미지만 전송)
	      if (state.uploadedFiles.length > 0) {
	        formData.append('image', state.uploadedFiles[0]);
	      }

	      const response = await fetch('/api/v1/content/publish', {
	        method: 'POST',
	        // ⚠️ 주의: FormData를 쓸 때는 headers에 'Content-Type'을 직접 적으면 안 됩니다! 
	        // 브라우저가 파일 경계(Boundary)를 알아서 설정하도록 비워두어야 합니다.
	        body: formData
	      });

	      if (!response.ok) throw new Error(`발행 서버 통신 오류: ${response.status}`);

	      const result = await response.json();
	      if (result.status === 'success') {
	        uiManager.showToast(`🚀 ${PLATFORM_CONFIG[sns].label}에 성공적으로 게시되었습니다!`);
	      } else {
	        uiManager.showToast(`⚠️ 게시 실패: ${result.message}`);
	      }
	    } catch (error) {
	      console.error('발행 통신 오류:', error);
	      uiManager.showToast('서버와의 통신 중 오류가 발생했습니다.');
	    } finally {
	      uiManager.toggleLoading(false);
	    }
	  };


/* --------------------------------------------------------------------------
   6. Initialization (초기화)
   -------------------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', async () => {
  // 폼의 기본 제출(새로고침) 동작 막기
  const form = document.getElementById('contentForm');
  if (form) {
    form.addEventListener('submit', (e) => e.preventDefault());
  }

  // 캘린더 데이터 세팅
  const dashboardData = await apiService.fetchDashboardData();
  initCalendar(dashboardData.events);
  
  try {
      const response = await fetch('/api/v1/content/pending');
      const data = await response.json();
      
      state.pendingPosts = data.map(item => ({
        id: `pending-${item.id}`,
        sns: item.platform,
        menu: item.menuName,
        bodyText: item.content,
        // PLATFORM_CONFIG에서 아이콘과 색상 매핑
        label: PLATFORM_CONFIG[item.platform].label,
        icon: PLATFORM_CONFIG[item.platform].icon,
        color: PLATFORM_CONFIG[item.platform].color
      }));
      
      renderPendingHTML(); // 화면 갱신
    } catch (err) {
      console.error("데이터 로드 실패:", err);
    }
});

/* --------------------------------------------------------------------------
   7. Helper Functions & UI Event Listeners
   -------------------------------------------------------------------------- */
function getSelectedKeywords() {
  return [...document.querySelectorAll('#keywordGroup input:checked')].map(e => e.value);
}

function getActiveTones() {
  return [...document.querySelectorAll('#toneGroup .tone-item.active')].map(e => e.dataset.tone);
}

function getActiveEmojiLevel() {
  return document.querySelector('#emojiGroup .slider-item.active')?.dataset.emoji || 'mid';
}

// 글자 수 슬라이더 (app.js 스타일 적용)
const rangeEl = document.getElementById('lengthRange');
const rangeValEl = document.getElementById('rangeVal');
function updateRange() {
  if (!rangeEl || !rangeValEl) return;
  const pct = ((rangeEl.value - rangeEl.min) / (rangeEl.max - rangeEl.min)) * 100;
  rangeEl.style.setProperty('--pct', pct + '%');
  rangeValEl.textContent = rangeEl.value + '자';
}
if (rangeEl) {
  rangeEl.addEventListener('input', updateRange);
  updateRange();
}

// 제목 글자 수 카운터 (app.js 스타일 적용)
const menuInput = document.getElementById('menuName');
const menuCount = document.getElementById('menuCount');
if(menuInput && menuCount) {
  menuInput.addEventListener('input', () => {
    const len = menuInput.value.length;
    menuCount.textContent = `${len}/50`;
    menuCount.classList.toggle('warn', len > 40);
  });
}

// 말투 스타일 선택 토글
document.querySelectorAll('#toneGroup .tone-item').forEach(item => {
  item.addEventListener('click', () => {
    item.classList.toggle('active');
    if (!document.querySelectorAll('#toneGroup .tone-item.active').length) {
      document.querySelector('#toneGroup .tone-item[data-tone="default"]')?.classList.add('active');
    }
  });
});

// 이모지 선택 라디오 버튼처럼 동작
document.querySelectorAll('#emojiGroup .slider-item').forEach(item => {
  item.addEventListener('click', () => {
    document.querySelectorAll('#emojiGroup .slider-item').forEach(i => i.classList.remove('active'));
    item.classList.add('active');
  });
});

/* --------------------------------------------------------------------------
   8. Output Tabs & SNS Chips (app.js 로직 적용)
   -------------------------------------------------------------------------- */
// 탭 전환 로직
const tabButtons = document.querySelectorAll('.tab-btn');
const panes = document.querySelectorAll('.output-pane');
tabButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    tabButtons.forEach(b => b.classList.remove('active'));
    panes.forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    
    const targetPane = document.getElementById('pane-' + btn.dataset.tab);
    if(targetPane) targetPane.classList.add('active');
  });
});

// SNS 칩 토글 로직
document.querySelectorAll('#snsChips .chip').forEach(chip => {
  chip.addEventListener('click', () => {
    chip.classList.toggle('active');
    if (chip.id === 'communityChip') {
      chip.style.background = chip.classList.contains('active') ? '#6366F1' : '';
      chip.style.color = chip.classList.contains('active') ? '#fff' : '#6366F1';
    }
    updateRetouchState();
  });
});

/* --------------------------------------------------------------------------
   9. Image Upload & Retouch Modal
   -------------------------------------------------------------------------- */
const uploadZone = document.getElementById('uploadZone');
const imgInput = document.getElementById('imgInput');

uploadZone?.addEventListener('click', () => imgInput && imgInput.click());
uploadZone?.addEventListener('dragover', e => { e.preventDefault(); uploadZone.style.borderColor = 'rgba(20,184,166,0.35)'; });
uploadZone?.addEventListener('dragleave', () => { uploadZone.style.borderColor = ''; });
uploadZone?.addEventListener('drop', e => {
  e.preventDefault(); uploadZone.style.borderColor = '';
  if (e.dataTransfer.files) handleFiles(e.dataTransfer.files);
});
imgInput?.addEventListener('change', () => handleFiles(imgInput.files));

function handleFiles(files) {
  if (!files) return;
  Array.from(files).slice(0, 5 - state.uploadedImages.length).forEach(file => {
    if (!file.type.startsWith('image/')) return;
    
    // 💡 실제 파일 객체를 배열에 저장!
    state.uploadedFiles.push(file);

    const reader = new FileReader();
    reader.onload = e => {
      state.uploadedImages.push(e.target.result);
      renderImagePreviews();
      updateRetouchState();
    };
    reader.readAsDataURL(file);
  });
}

function renderImagePreviews() {
  const previewContainer = document.getElementById('imgPreviews');
  if (!previewContainer) return;
  previewContainer.innerHTML = state.uploadedImages.map((src, i) => 
    `<div class="img-thumb-wrap"><img class="img-thumb" src="${src}"><button class="remove-btn" type="button" onclick="removeImg(${i})">×</button></div>`
  ).join('');
}

window.removeImg = function(i) {
  state.uploadedImages.splice(i, 1);
  state.uploadedFiles.splice(i, 1); // 삭제할 때 파일 원본도 같이 삭제!
  renderImagePreviews();
  updateRetouchState();
};

function updateRetouchState() {
  const activeSNS = [...document.querySelectorAll('#snsChips .chip.active')].map(c => c.dataset.sns);
  const enabled = (activeSNS.includes('instagram') || activeSNS.includes('facebook')) && state.uploadedImages.length > 0;
  const btn = document.getElementById('retouchBtn');
  if(btn) btn.disabled = !enabled;
}

window.openRetouchModal = function() {
  const modal = document.getElementById('retouchModal');
  if (modal) modal.classList.add('open');
};
window.closeRetouchModal = function() { 
  const modal = document.getElementById('retouchModal');
  if (modal) modal.classList.remove('open'); 
};
window.applyRetouchAndClose = function() { closeRetouchModal(); };

/* --------------------------------------------------------------------------
   10. Calendar & Drag-and-Drop (스케줄링 기능)
   -------------------------------------------------------------------------- */
   function initCalendar(eventsData) {
     const calEl = document.getElementById('fullcalendar');
     if(!calEl || typeof FullCalendar === 'undefined') return;

     state.calendarInstance = new FullCalendar.Calendar(calEl, {
       initialView: 'dayGridMonth',
       locale: 'ko',
       headerToolbar: { left: 'prev', center: 'title', right: 'next' },
       height: 'auto',
       editable: true,
       droppable: true,
       events: eventsData,
       eventDrop: (info) => {
         const newDate = info.event.startStr.slice(0, 10);
         uiManager.showToast(`📅 일정이 ${newDate}(으)로 이동됐습니다.`);
       },
       // 💡 클릭 시 모달창 열기 부활!
       eventClick: (info) => {
         openEventModal(info);
       }
     });
     state.calendarInstance.render();
     setupCalendarDropZones();
   }

window.toggleCalendarExpand = function() {
  const grid = document.getElementById('dashboardGrid');
  if (grid) grid.classList.toggle('calendar-expanded');
  setTimeout(() => state.calendarInstance?.updateSize(), 460);
};

function addToPending(sns, menu, bodyText) {
  const platform = PLATFORM_CONFIG[sns] || { label: sns, icon: '📌', color: '#ccc' };
  const post = {
    id: `pending-${sns}-${Date.now()}`,
    sns, menu, bodyText,
    label: platform.label,
    icon: platform.icon,
    color: platform.color
  };
  state.pendingPosts.push(post);
  renderPendingHTML();
}

function renderPendingHTML() {
  const list = document.getElementById('pendingList');
  const badge = document.getElementById('pendingCountBadge');
  
  if(badge) badge.textContent = state.pendingPosts.length;
  if (!list) return;
  
  if (!state.pendingPosts.length) {
    list.innerHTML = `<div class="pending-empty"><div class="pe-icon">📭</div><p>콘텐츠를 생성하면<br>여기에 표시됩니다.</p></div>`;
    return;
  }
  
  list.innerHTML = state.pendingPosts.map(post => `
    <div class="pending-item" id="${post.id}" draggable="true" ondragstart="onPendingDragStart(event,'${post.id}')" ondragend="onPendingDragEnd(event)">
      <span class="pi-dot" style="background:${post.color};"></span>
      <div class="pi-info">
        <div class="pi-name">${post.icon} ${post.menu}</div>
        <div class="pi-platform">${post.label}</div>
      </div>
      <div class="pi-drag-hint">드래그</div>
      <button class="pi-remove-btn" type="button" onclick="removePending('${post.id}')">✕</button>
    </div>
  `).join('');
}

window.removePending = function(id) {
  state.pendingPosts = state.pendingPosts.filter(p => p.id !== id);
  renderPendingHTML();
};

let draggedPendingItem = null;
window.onPendingDragStart = function(event, id) {
  draggedPendingItem = state.pendingPosts.find(p => p.id === id);
  event.dataTransfer.effectAllowed = 'move';
  document.getElementById(id)?.classList.add('dragging');
};
window.onPendingDragEnd = function(event) {
  draggedPendingItem = null;
  document.querySelectorAll('.pending-item.dragging').forEach(el => el.classList.remove('dragging'));
  document.querySelectorAll('.fc-day.drag-over').forEach(el => el.classList.remove('drag-over'));
};

function setupCalendarDropZones() {
  const calEl = document.getElementById('fullcalendar');
  if(!calEl) return;
  
  calEl.addEventListener('dragover', e => {
    e.preventDefault();
    const dayCell = e.target.closest('.fc-daygrid-day, .fc-day');
    if (dayCell) dayCell.classList.add('drag-over');
  });
  
  calEl.addEventListener('dragleave', e => {
    const dayCell = e.target.closest('.fc-daygrid-day, .fc-day');
    if (dayCell) dayCell.classList.remove('drag-over');
  });

  calEl.addEventListener('drop', e => {
    e.preventDefault();
    document.querySelectorAll('.fc-day.drag-over').forEach(el => el.classList.remove('drag-over'));
	
    const dayCell = e.target.closest('.fc-day');
    if (!draggedPendingItem || !state.calendarInstance || !dayCell) return;
    
    const dateStr = dayCell.getAttribute('data-date');
    const post = draggedPendingItem;
    
	state.calendarInstance.addEvent({
	      id: `sched-${post.sns}-${Date.now()}`,
	      title: `${post.icon} ${post.menu}`,
	      start: `${dateStr}T12:00:00`, 
	      classNames: [`fc-event-${post.sns === 'community' ? 'comm' : post.sns.substring(0,2)}`],
	      
	      // 💡 잃어버렸던 포스트 원본 데이터 저장소 부활!
	      extendedProps: { 
	        sns: post.sns, 
	        bodyText: post.bodyText, 
	        menu: post.menu 
	      }
	    });
    
    removePending(post.id);
    uiManager.showToast(`📅 ${post.label} 포스트가 ${dateStr}에 스케줄링 되었습니다.`);
  });
}


/* --------------------------------------------------------------------------
   이미지 보정(필터) 기능
   -------------------------------------------------------------------------- */
window.applyFilter = function(filterType) {
  const previewImg = document.getElementById('retouchPreviewImg');
  if (!previewImg) return;

  state.currentFilter = filterType;
  
  // 선택한 필터에 따라 CSS filter 속성을 직접 제어
  switch(filterType) {
    case 'grayscale': previewImg.style.filter = 'grayscale(100%)'; break;
    case 'sepia':     previewImg.style.filter = 'sepia(80%)'; break;
    case 'blur':      previewImg.style.filter = 'blur(2px)'; break;
    case 'bright':    previewImg.style.filter = 'brightness(1.2) contrast(1.1)'; break;
    default:          previewImg.style.filter = 'none'; break;
  }
};

/* --------------------------------------------------------------------------
   캘린더 미리보기 모달 제어
   -------------------------------------------------------------------------- */
window.openEventModal = function(info) {
  const modal = document.getElementById('previewModal');
  if (!modal) return;

  // 모달 내부에 데이터 채워넣기 (HTML의 ID에 맞게 수정 가능합니다)
  const props = info.event.extendedProps;
  
  // 예: 모달의 제목과 본문을 표시할 HTML 요소가 있다면 값을 넣어줍니다.
  const modalTitle = document.getElementById('modalTitle'); // 제목 요소 ID
  const modalBody = document.getElementById('modalBody');   // 본문 요소 ID

  if (modalTitle) modalTitle.textContent = info.event.title;
  if (modalBody) {
    // 줄바꿈을 유지하며 출력하기 위해 innerHTML 사용
    modalBody.innerHTML = (props.bodyText || '내용이 없습니다.').replace(/\n/g, '<br>');
  }

  modal.classList.remove('hidden');
};

window.closeEventModal = function() {
  document.getElementById('previewModal')?.classList.add('hidden');
};

/* --------------------------------------------------------------------------
   DB에서 저장된 콘텐츠 불러오기
   -------------------------------------------------------------------------- */
window.loadPendingFromDB = async function() {
  uiManager.showToast("⏳ 저장된 데이터를 불러오는 중...");
  
  try {
    const response = await fetch('/api/v1/content/pending');
    if (!response.ok) throw new Error("데이터 로드 실패");
    
    const dbData = await response.json();
    
    // 서버 데이터를 프론트엔드 state 형식으로 변환
    const loadedPosts = dbData.map(item => {
      // 쉼표로 저장된 해시태그를 다시 배열로 변환
      const tagArray = item.hashtags ? item.hashtags.split(',').map(t => t.trim()) : [];
      
      return {
        id: `db-${item.id}`,
        sns: item.platform,
        menu: item.menuName,
        bodyText: item.content,
        hashtags: tagArray,
        imageUrl: item.imageUrl,
        originUrl: item.originUrl,
        label: PLATFORM_CONFIG[item.platform]?.label || item.platform,
        icon: PLATFORM_CONFIG[item.platform]?.icon || '📄',
        color: PLATFORM_CONFIG[item.platform]?.color || '#cbd5e1'
      };
    });

    // 기존 목록에 합치거나 새로 교체 (여기서는 교체로 진행)
    state.pendingPosts = loadedPosts;
    
    // 화면 갱신 함수 호출 (기존에 구현된 함수)
    renderPendingHTML();
    
    uiManager.showToast(`✅ ${loadedPosts.length}개의 포스트를 불러왔습니다.`);
  } catch (error) {
    console.error(error);
    uiManager.showToast("❌ 데이터를 불러오지 못했습니다.");
  }
};

/* --------------------------------------------------------------------------
   1. 플랫폼 노출 필터링 함수
   -------------------------------------------------------------------------- */
function updatePlatformVisibility() {
  const platforms = ['instagram', 'facebook', 'naver', 'kakao', 'community'];
  let firstVisible = null;

  platforms.forEach(sns => {
    const pane = document.getElementById(`pane-${sns}`);
    const tab = document.querySelector(`.tab-btn[data-target="pane-${sns}"]`); // 탭 버튼도 있다고 가정
    
    // 해당 플랫폼에 생성된 콘텐츠가 있는지 확인
    const hasContent = state.generatedContent && state.generatedContent[sns];

    if (hasContent) {
      if (pane) pane.style.display = 'block';
      if (tab) tab.style.display = 'flex';
      if (!firstVisible) firstVisible = sns; // 가장 첫 번째로 보이는 플랫폼 저장
    } else {
      if (pane) pane.style.display = 'none';
      if (tab) tab.style.display = 'none';
    }
  });

  // 아무것도 없다면 인스타그램을 기본으로 보여줌 (Empty State)
  if (!firstVisible) {
    const igPane = document.getElementById('pane-instagram');
    if (igPane) igPane.style.display = 'block';
  } else {
    // 첫 번째로 유효한 플랫폼 탭을 강제로 활성화 (선택 사항)
    // switchTab(firstVisible); 
  }
}

/* --------------------------------------------------------------------------
   2. center-col 전용 기록 불러오기 함수
   -------------------------------------------------------------------------- */
window.loadCenterHistory = async function() {
  uiManager.showToast("⏳ 최근 기록을 불러오는 중...");
  
  try {
    const response = await fetch('/api/v1/content/pending');
    const data = await response.json();
    
    if (data.length === 0) return uiManager.showToast("저장된 기록이 없습니다.");

    // 가장 최근의 생성 그룹(동일한 menuName 또는 시간대)을 찾거나 
    // 여기서는 간단하게 모든 플랫폼 데이터를 state에 로드합니다.
    state.generatedContent = {}; // 기존 데이터 초기화

    data.forEach(item => {
      state.generatedContent[item.platform] = {
        text: item.content,
        hashtags: item.hashtags ? item.hashtags.split(',') : []
      };
      
      // UI에 데이터 꽂아넣기
      const textEl = document.getElementById(`text-${item.platform}`);
      const resultDiv = document.getElementById(`result-${item.platform}`);
      const emptyDiv = document.getElementById(`empty-${item.platform}`);
      
      if (textEl) textEl.textContent = item.content;
      if (resultDiv) resultDiv.style.display = 'block';
      if (emptyDiv) emptyDiv.style.display = 'none';
    });

    // 💡 생성된 플랫폼만 보이도록 필터링 실행!
    updatePlatformVisibility();
    
    uiManager.showToast("✅ 과거 기록이 로드되었습니다.");
  } catch (err) {
    uiManager.showToast("❌ 기록 로드 실패");
  }
};

// 💡 기존 generateContent의 성공 콜백 마지막에도 updatePlatformVisibility(); 를 추가해 주세요!


/* --------------------------------------------------------------------------
   중앙 영역용 기록 불러오기 (개선판)
   -------------------------------------------------------------------------- */
   
   /*

// 1. 모달 열고 목록 가져오기
window.loadCenterHistory = async function() {
  const container = document.getElementById('historyContainer');
  if(!container) return;
  
  container.innerHTML = '<p style="text-align:center; padding:20px;">불러오는 중...</p>';
  document.getElementById('historyListModal').classList.remove('hidden');

  try {
    const response = await fetch('/api/v1/content/pending');
    const data = await response.json();
    
    if (data.length === 0) {
      container.innerHTML = '<p style="text-align:center; padding:20px;">기록이 없습니다.</p>';
      return;
    }

    // 목록 생성
    container.innerHTML = data.map(item => `
      <div class="history-card" onclick='applyHistoryItem(${JSON.stringify(item).replace(/'/g, "&apos;")})'>
        <div class="history-info">
          <img src="${item.imageUrl || 'https://via.placeholder.com/50?text=No+Img'}" class="history-thumb">
          <div class="history-details">
            <h4>${item.menuName}</h4>
            <p><span class="platform-dot" style="background:${PLATFORM_CONFIG[item.platform].color}"></span>${PLATFORM_CONFIG[item.platform].label} · ${new Date(item.createdAt).toLocaleDateString()}</p>
          </div>
        </div>
        <div style="color:#6366f1; font-size:12px; font-weight:600;">선택 →</div>
      </div>
    `).join('');

  } catch (err) {
    uiManager.showToast("❌ 기록을 불러오지 못했습니다.");
  }
};

// 2. 선택한 기록을 실제 화면(Center-col)에 적용
window.applyHistoryItem = function(item) {
  // state 업데이트 (텍스트 + 해시태그)
  if (!state.generatedContent) state.generatedContent = {};
  state.generatedContent[item.platform] = {
    text: item.content,
    hashtags: item.hashtags ? item.hashtags.split(',') : []
  };

  // 💡 [핵심] 이미지 URL 복구
  if (item.imageUrl) {
    // state.uploadedImages의 첫 번째 값을 이 URL로 교체하여 프리뷰에 띄움
    state.uploadedImages = [item.imageUrl];
    // 만약 화면에 이미지 프리뷰 함수가 있다면 호출
    if (typeof renderImagePreviews === 'function') renderImagePreviews();
  }

  // UI 요소에 텍스트 주입
  const textEl = document.getElementById(`text-${item.platform}`);
  const resultDiv = document.getElementById(`result-${item.platform}`);
  const emptyDiv = document.getElementById(`empty-${item.platform}`);
  
  if (textEl) textEl.textContent = item.content;
  if (resultDiv) resultDiv.style.display = 'block';
  if (emptyDiv) emptyDiv.style.display = 'none';

  // 생성된 플랫폼만 보이도록 필터링
  updatePlatformVisibility();
  
  closeHistoryModal();
  uiManager.showToast(`✅ '${item.menuName}' (${item.platform}) 기록을 불러왔습니다.`);
};

window.closeHistoryModal = function() {
  document.getElementById('historyListModal').classList.add('hidden');
};

*/