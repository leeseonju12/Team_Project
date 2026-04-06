/* ==========================================================================
   1. Config & Mock Data
   ========================================================================== */
const PLATFORM_CONFIG = {
  instagram: { label: 'Instagram', color: '#E1306C', icon: '📷' },
  facebook:  { label: 'Facebook', color: '#1877F2', icon: '📘' },
  naver:     { label: '네이버 블로그', color: '#03C75A', icon: '🟢' },
  kakao:     { label: '카카오 채널', color: '#FEE500', icon: '💛' },
  community: { label: '커뮤니티', color: '#6366F1', icon: '💬' }
};

// 문법 오류를 일으키던 [...] 제거 및 빈 배열/객체로 초기화
const MOCK_DATA = {
  comments: [], 
  calendarEvents: [], 
  communities: {} 
};

/* ==========================================================================
   2. State Management
   ========================================================================== */
const state = {
  uploadedImages: [],
  communityTags: [],
  currentFilter: 'none',
  appliedFilter: 'none',
  generatedContent: {}, 
  scheduledEvents: [],  
  pendingPosts: [],     
  calendarInstance: null
};

/* ==========================================================================
   3. API Service Layer
   ========================================================================== */
const apiService = {
  async generateContent(params, onStreamChunk) {
    try {
      console.log('서버로 전송될 생성 파라미터:', params);
      return await mockAIStreamCall(params, onStreamChunk); 
    } catch (error) {
      console.error('AI 생성 API 오류:', error);
      throw error;
    }
  },

  async fetchDashboardData() {
    return {
      events: MOCK_DATA.calendarEvents,
      pending: []
    };
  },

  async publishToChannel(snsType, contentData) {
    console.log(`[${snsType}] 발행 요청:`, contentData);
    return { success: true };
  }
};

/* ==========================================================================
   4. UI Manager (비어있던 함수들 채워넣음!)
   ========================================================================== */
const uiManager = {
  showToast(msg) { 
    // 실제 토스트 UI가 없다면 alert로 임시 동작하게 처리
    console.log("[Toast 알림]:", msg);
    alert(msg); 
  },
  
  toggleLoading(isLoading) {
    const btn = document.getElementById('generateBtn');
    const progress = document.getElementById('genProgressBar');
    if (!btn) return;
    
    if (isLoading) {
      btn.classList.add('loading');
      progress?.classList.add('active');
    } else {
      btn.classList.remove('loading');
      progress?.classList.remove('active');
    }
  },

  renderPendingList() { 
    if (typeof renderPendingHTML === 'function') renderPendingHTML(); 
  },
  
  updatePreview(sns, data) { 
    // 미리보기 텍스트 요소가 있다면 업데이트
    const previewEl = document.getElementById(`preview-${sns}`);
    if (previewEl) previewEl.innerText = data.text;
  },
  
  createStreamRenderer(textEl, cursorEl) { 
    // 스트리밍 애니메이션이 작동하도록 실제 로직 추가
    textEl.innerHTML = '';
    textEl.appendChild(cursorEl);
    return (char) => {
      const textNode = document.createTextNode(char);
      textEl.insertBefore(textNode, cursorEl);
    };
  }
};

/* ==========================================================================
   5. Core Logic & Event Controllers
   ========================================================================== */
async function handleGenerateContent() {
  const menuEl = document.getElementById('menuName');
  const extraEl = document.getElementById('extraInfo');
  const lengthRange = document.getElementById('lengthRange');
  
  const menu = menuEl ? menuEl.value.trim() : '';
  const extra = extraEl ? extraEl.value.trim() : '';
  const activeSNS = [...document.querySelectorAll('#snsChips .chip.active')].map(c => c.dataset.sns);

  if (!menu || !activeSNS.length) {
    uiManager.showToast('필수 값(메뉴명)을 입력하고 SNS를 선택해주세요.');
    return;
  }

  const requestParams = {
    menuName: menu,
    extraInfo: extra,
    keywords: getSelectedKeywords(),
    tones: getActiveTones(),
    emojiLevel: getActiveEmojiLevel(),
    maxLen: lengthRange ? +lengthRange.value : 500,
    targetPlatforms: activeSNS,
    imageCount: state.uploadedImages.length
  };

  uiManager.toggleLoading(true);
  state.generatedContent = {};

  try {
    const generatePromises = activeSNS.map(async (sns) => {
      const textEl = document.getElementById(`text-${sns}`);
      if (!textEl) return;

      const cursor = document.createElement('span');
      cursor.className = 'stream-cursor';
      const streamRenderer = uiManager.createStreamRenderer(textEl, cursor);

      const platformParams = { ...requestParams, platform: sns };
      const result = await apiService.generateContent(platformParams, streamRenderer);

      state.generatedContent[sns] = result;
      uiManager.updatePreview(sns, result);
      addToPending(sns, menu, result.text);
    });

    await Promise.all(generatePromises);
    uiManager.showToast('AI 콘텐츠 생성이 완료되었습니다.');

  } catch (err) {
    uiManager.showToast('생성 중 오류가 발생했습니다.');
  } finally {
    uiManager.toggleLoading(false);
  }
}

async function handlePublish(sns) {
  const content = state.generatedContent[sns];
  if (!content) return uiManager.showToast('먼저 콘텐츠를 생성해주세요.');

  try {
    await apiService.publishToChannel(sns, content);
    uiManager.showToast(`🚀 ${PLATFORM_CONFIG[sns].label} 게시 완료!`);
  } catch (error) {
    uiManager.showToast('발행에 실패했습니다.');
  }
}

/* ==========================================================================
   6. Initialization (초기화)
   ========================================================================== */
document.addEventListener('DOMContentLoaded', async () => {
  const genBtn = document.getElementById('generateBtn');
  if (genBtn) genBtn.addEventListener('click', handleGenerateContent);
  
  const dashboardData = await apiService.fetchDashboardData();
  initCalendar(dashboardData.events);
});

/* ==========================================================================
   7. Helper Functions & Form Controls
   ========================================================================== */
function getSelectedKeywords() {
  return [...document.querySelectorAll('#keywordGroup input:checked')].map(e => e.value);
}

function getActiveTones() {
  return [...document.querySelectorAll('#toneGroup .tone-item.active')].map(e => e.dataset.tone);
}

function getActiveEmojiLevel() {
  return document.querySelector('#emojiGroup .slider-item.active')?.dataset.emoji || 'mid';
}

const rangeEl = document.getElementById('lengthRange');
function updateRange() {
  if (!rangeEl) return;
  const p = ((rangeEl.value - 100) / 400) * 100;
  rangeEl.style.setProperty('--pct', p + '%');
  const rangeValEl = document.getElementById('rangeVal');
  if (rangeValEl) rangeValEl.textContent = rangeEl.value + '자';
}
if (rangeEl) {
  rangeEl.addEventListener('input', updateRange);
  updateRange();
}

document.getElementById('menuName')?.addEventListener('input', (e) => {
  const l = e.target.value.length;
  const el = document.getElementById('menuCount');
  if (!el) return;
  el.textContent = `${l}/50`;
  el.classList.toggle('warn', l > 40);
});

document.querySelectorAll('#toneGroup .tone-item').forEach(item => {
  item.addEventListener('click', () => {
    item.classList.toggle('active');
    if (!document.querySelectorAll('#toneGroup .tone-item.active').length) {
      const defaultTone = document.querySelector('#toneGroup .tone-item[data-tone="default"]');
      if (defaultTone) defaultTone.classList.add('active');
    }
  });
});

document.querySelectorAll('#emojiGroup .slider-item').forEach(item => {
  item.addEventListener('click', () => {
    document.querySelectorAll('#emojiGroup .slider-item').forEach(i => i.classList.remove('active'));
    item.classList.add('active');
  });
});

/* ==========================================================================
   8. Community Tags & Autocomplete
   ========================================================================== */
const tagInput = document.getElementById('communityTagInput');
const acDropdown = document.getElementById('autocompleteDropdown');
const tagPillsEl = document.getElementById('tagPills');

function renderTagPills() {
  if (!tagPillsEl) return;
  tagPillsEl.innerHTML = state.communityTags.map(t => 
    `<span class="tag-pill">${t}<button class="remove-tag" onclick="removeTag('${t}')">✕</button></span>`
  ).join('');
}

function addTag(val) {
  const tag = val.trim().replace(/,/g, '');
  if (!tag || state.communityTags.includes(tag)) return;
  state.communityTags.push(tag);
  renderTagPills();
  tagInput.value = '';
}

window.removeTag = function(tag) {
  state.communityTags = state.communityTags.filter(t => t !== tag);
  renderTagPills();
};

tagInput?.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    e.preventDefault();
    addTag(tagInput.value);
    if (acDropdown) acDropdown.classList.remove('open');
  } else if (e.key === 'Backspace' && !tagInput.value && state.communityTags.length) {
    state.communityTags.pop();
    renderTagPills();
  }
});
document.getElementById('tagInputWrap')?.addEventListener('click', () => tagInput?.focus());

/* ==========================================================================
   9. Image Upload & Retouch Modal
   ========================================================================== */
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
    `<div class="img-thumb-wrap"><img class="img-thumb" src="${src}"><button class="remove-btn" onclick="removeImg(${i})">×</button></div>`
  ).join('');
}

window.removeImg = function(i) {
  state.uploadedImages.splice(i, 1);
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
  
  const img = document.getElementById('retouchPreviewImg');
  const placeholder = document.getElementById('retouchPlaceholder');
  if(state.uploadedImages.length && img && placeholder) {
    img.src = state.uploadedImages[0];
    img.style.display = 'block';
    placeholder.style.display = 'none';
  }
};

window.closeRetouchModal = function() { 
  const modal = document.getElementById('retouchModal');
  if (modal) modal.classList.remove('open'); 
};
window.applyRetouchAndClose = function() { closeRetouchModal(); };

/* ==========================================================================
   10. Output Tabs & SNS Chips
   ========================================================================== */
document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    const tabName = btn.dataset.tab;
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === tabName));
    document.querySelectorAll('.output-pane').forEach(p => p.classList.toggle('active', p.id === 'pane-' + tabName));
  });
});

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

/* ==========================================================================
   11. Calendar & Drag-and-Drop
   ========================================================================== */
function initCalendar(eventsData) {
  const calEl = document.getElementById('fullcalendar');
  // FullCalendar 라이브러리가 로드되지 않았거나 요소가 없으면 무시
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
    eventClick: (info) => {
      openEventModal(info);
    }
  });
  state.calendarInstance.render();
  setupCalendarDropZones();
}

function toggleCalendarExpand() {
  const grid = document.getElementById('dashboardGrid');
  if (grid) grid.classList.toggle('calendar-expanded');
  setTimeout(() => state.calendarInstance?.updateSize(), 460);
}

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
      <button class="pi-remove-btn" onclick="removePending('${post.id}')">✕</button>
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
      extendedProps: { sns: post.sns, bodyText: post.bodyText, menu: post.menu }
    });
    
    removePending(post.id);
    uiManager.showToast(`📅 ${post.label} 포스트가 ${dateStr}에 등록됐습니다!`);
  });
}

/* ==========================================================================
   12. Mock API & Streamer
   ========================================================================== */
async function mockAIStreamCall(params, onStreamChunk) {
  return new Promise((resolve) => {
    const mockResponse = `안녕하세요! AI가 생성한 [${params.platform}]용 '${params.menuName}' 마케팅 문구입니다.\n키워드: ${params.keywords.join(', ')}\n\n이 텍스트는 API 연결 전 화면을 테스트하기 위한 목업(Mock) 텍스트입니다. 정상 작동합니다!`;
    
    let i = 0;
    const interval = setInterval(() => {
      if (onStreamChunk) onStreamChunk(mockResponse[i]);
      i++;
      if (i >= mockResponse.length) {
        clearInterval(interval);
        resolve({ text: mockResponse });
      }
    }, 20); 
  });
}

function openEventModal(info) {
  const modal = document.getElementById('previewModal');
  if(modal) modal.classList.remove('hidden');
}

window.closeEventModal = function() {
  document.getElementById('previewModal')?.classList.add('hidden');
};