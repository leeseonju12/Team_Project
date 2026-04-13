/* ==========================================================================
   app.js
   소셜다모아 통합 Frontend Controller
   ========================================================================== */

/* --------------------------------------------------------------------------
   1. Config
   -------------------------------------------------------------------------- */
const PLATFORM_CONFIG = {
  instagram: { label: 'Instagram', color: '#E1306C' },
  facebook: { label: 'Facebook', color: '#1877F2' },
  naver: { label: '네이버 블로그', color: '#03C75A' },
  kakao: { label: '카카오 채널', color: '#FEE500' },
  community: { label: '커뮤니티', color: '#6366F1' }
};

const PASTEL_PALETTE = {
  instagram: { backgroundColor: '#fce4ec', textColor: '#880e4f', borderColor: '#e1306c' },
  facebook: { backgroundColor: '#e3f0fd', textColor: '#0d47a1', borderColor: '#1877f2' },
  naver: { backgroundColor: '#e6f9ee', textColor: '#1b5e20', borderColor: '#03c75a' },
  kakao: { backgroundColor: '#fffde7', textColor: '#4a3000', borderColor: '#fee500' },
  community: { backgroundColor: '#ede9fe', textColor: '#3730a3', borderColor: '#6366f1' }
};

/* --------------------------------------------------------------------------
   2. State
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

  async publishContent(sns, text, imageFile) {
    const formData = new FormData();
    formData.append('platform', sns);
    formData.append('text', text);

    if (imageFile) {
      formData.append('image', imageFile);
    }

    const response = await fetch('/api/posts/publish', {
      method: 'POST',
      // 브라우저가 Boundary를 자동 설정하도록 Content-Type 헤더 생략
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

    modal.classList.remove('hidden');
  },

  closeEventModal() {
    document.getElementById('previewModal')?.classList.add('hidden');
  }
};

window.openEventModal = uiManager.openEventModal;
window.closeEventModal = uiManager.closeEventModal;

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
      maxLength: document.getElementById('lengthRange') ? parseInt(document.getElementById('lengthRange').value, 10) : 300
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
    uiManager.showToast('게시할 SNS를 1개 이상 선택해 주세요.');
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
    const tab = document.querySelector(`.tab-btn[data-target="pane-${sns}"]`);
    
    const hasContent = state.generatedContent && state.generatedContent[sns];

    if (hasContent) {
      if (pane) pane.style.display = 'block';
      if (tab) tab.style.display = 'flex';
      if (!firstVisible) firstVisible = sns;
    } else {
      if (pane) pane.style.display = 'none';
      if (tab) tab.style.display = 'none';
    }
  });

  if (!firstVisible) {
    const igPane = document.getElementById('pane-instagram');
    if (igPane) igPane.style.display = 'block';
  }
}

function animateGeneratedText(sns, text, menu) {
  const textEl = document.getElementById(`text-${sns}`);
  if (!textEl) return;

  const cursor = document.createElement('span');
  cursor.className = 'stream-cursor';
  const streamRenderer = uiManager.createStreamRenderer(textEl, cursor);

  let index = 0;
  const interval = setInterval(() => {
    if (text[index]) streamRenderer(text[index]);
    index += 1;

    if (index >= text.length) {
      clearInterval(interval);
      uiManager.updatePreview(sns);
      addToPending(sns, menu, text);
      updatePlatformVisibility();
    }
  }, 15);
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

    requestMeta.activeSNS.forEach((sns) => {
      const generatedText = responseData[sns] || '해당 플랫폼의 생성 결과가 없습니다.';
      state.generatedContent[sns] = { text: generatedText };
      animateGeneratedText(sns, generatedText, requestMeta.menu);
    });

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

  uiManager.toggleLoading(true);
  uiManager.showToast(`${PLATFORM_CONFIG[sns]?.label || sns}에 게시 중입니다. 잠시만 기다려주세요...`);

  try {
    const result = await apiService.publishContent(
      sns,
      content.text,
      state.uploadedFiles[0] || null
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

/* --------------------------------------------------------------------------
   7. Pending Posts & Data Synchronization
   -------------------------------------------------------------------------- */
function addToPending(sns, menu, bodyText) {
  const platform = PLATFORM_CONFIG[sns] || { label: sns, color: '#6366F1', icon: '📌' };

  state.pendingPosts.push({
    id: `pending-${sns}-${Date.now()}`,
    sns,
    menu,
    bodyText,
    label: platform.label,
    color: platform.color,
    icon: platform.icon || '📌'
  });

  renderPendingHTML();
}

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
    .map(
      (post) => `
        <div class="pending-item" id="${post.id}" draggable="true"
             ondragstart="onPendingDragStart(event,'${post.id}')"
             ondragend="onPendingDragEnd(event)">
          <span class="pi-dot" style="background:${post.color};"></span>
          <div class="pi-info">
            <div class="pi-name">${post.icon ? post.icon + ' ' : ''}${post.menu}</div>
            <div class="pi-platform">${post.label}</div>
          </div>
          <div class="pi-drag-hint">드래그</div>
          <button class="pi-remove-btn" type="button" onclick="removePending('${post.id}')">✕</button>
        </div>
      `
    )
    .join('');
}

window.removePending = function (id) {
  if (typeof CalendarManager !== 'undefined' && CalendarManager.removePending) {
    CalendarManager.removePending(id);
  } else {
    state.pendingPosts = state.pendingPosts.filter(p => p.id !== id);
    renderPendingHTML();
  }
};

window.loadPendingFromDB = async function() {
  uiManager.showToast("저장된 데이터를 불러오는 중...");
  
  try {
    const response = await fetch('/api/posts/pending');
    if (!response.ok) throw new Error("데이터 로드 실패");
    
    const dbData = await response.json();
    
    const loadedPosts = dbData.map(item => {
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

    state.pendingPosts = loadedPosts;
    renderPendingHTML();
    
    uiManager.showToast(`${loadedPosts.length}개의 포스트를 불러왔습니다.`);
  } catch (error) {
    console.error(error);
    uiManager.showToast("데이터를 불러오지 못했습니다.");
  }
};

window.loadCenterHistory = async function() {
  uiManager.showToast("최근 기록을 불러오는 중...");
  
  try {
    const response = await fetch('/api/posts/pending');
    const data = await response.json();
    
    if (data.length === 0) return uiManager.showToast("저장된 기록이 없습니다.");

    state.generatedContent = {}; 

    data.forEach(item => {
      state.generatedContent[item.platform] = {
        text: item.content,
        hashtags: item.hashtags ? item.hashtags.split(',') : []
      };
      
      const textEl = document.getElementById(`text-${item.platform}`);
      const resultDiv = document.getElementById(`result-${item.platform}`);
      const emptyDiv = document.getElementById(`empty-${item.platform}`);
      
      if (textEl) textEl.textContent = item.content;
      if (resultDiv) resultDiv.style.display = 'block';
      if (emptyDiv) emptyDiv.style.display = 'none';
    });

    updatePlatformVisibility();
    uiManager.showToast("과거 기록이 로드되었습니다.");
  } catch (err) {
    uiManager.showToast("기록 로드 실패");
  }
};

/* --------------------------------------------------------------------------
   8. Upload / Retouch
   -------------------------------------------------------------------------- */
function handleFiles(files) {
  if (!files) return;

  Array.from(files)
    .slice(0, 5 - state.uploadedImages.length)
    .forEach((file) => {
      if (!file.type.startsWith('image/')) return;

      state.uploadedFiles.push(file);

      const reader = new FileReader();
      reader.onload = (e) => {
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

  previewContainer.innerHTML = state.uploadedImages
    .map(
      (src, i) => `
        <div class="img-thumb-wrap">
          <img class="img-thumb" src="${src}" alt="">
          <button class="remove-btn" type="button" onclick="removeImg(${i})">×</button>
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
  state.uploadedImages.splice(index, 1);
  state.uploadedFiles.splice(index, 1);
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
  const dashboardData = await apiService.fetchDashboardData();

  if (typeof CalendarManager !== 'undefined') {
    CalendarManager.init({
      state,
      uiManager,
      PLATFORM_CONFIG,
      PASTEL_PALETTE,
      renderPendingHTML,
      publishPost: window.publishPost
    });
    CalendarManager.initCalendar();
  }

  mergePendingPosts(dashboardData.pending);
  renderPendingHTML();
  updateRetouchState();
}

/* --------------------------------------------------------------------------
   11. Boot
   -------------------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', async () => {
  bindAllUIEvents();
  await initializeDashboard();
});