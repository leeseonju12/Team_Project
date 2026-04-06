
//1. 탭 전환 로직
  const tabButtons = document.querySelectorAll('.tab-btn');
  const panes = document.querySelectorAll('.output-pane');
  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      tabButtons.forEach(b => b.classList.remove('active'));
      panes.forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById('pane-' + btn.dataset.tab).classList.add('active');
    });
  });

  // 2. SNS 및 말투 선택 토글
  document.querySelectorAll('.chip').forEach(chip => {
    chip.addEventListener('click', () => chip.classList.toggle('active'));
  });
  document.querySelectorAll('#toneGroup .tone-item').forEach(item => {
    item.addEventListener('click', () => {
      item.classList.toggle('active');
      if (document.querySelectorAll('#toneGroup .tone-item.active').length === 0) {
        document.querySelector('#toneGroup .tone-item[data-tone="default"]')?.classList.add('active');
      }
    });
  });

  // 3. 글자 수 슬라이더
  const rangeEl = document.getElementById('lengthRange');
  const rangeVal = document.getElementById('rangeVal');
  function updateRange() {
    const pct = ((rangeEl.value - rangeEl.min) / (rangeEl.max - rangeEl.min)) * 100;
    rangeEl.style.setProperty('--pct', pct + '%');
    rangeVal.textContent = rangeEl.value + '자';
  }
  if(rangeEl) {
    rangeEl.addEventListener('input', updateRange);
    updateRange();
  }

  // 4. 글자 수 카운터
  const menuInput = document.getElementById('menuName');
  const menuCount = document.getElementById('menuCount');
  if(menuInput) {
    menuInput.addEventListener('input', () => {
      const len = menuInput.value.length;
      menuCount.textContent = len + '/50';
      menuCount.classList.toggle('warn', len > 40);
    });
  }

  // 5. 텍스트 복사 기능
  function copyText(evt, sns) {
    const el = document.getElementById('text-' + sns);
    if (!el || !el.textContent) return;
    navigator.clipboard.writeText(el.textContent).then(() => {
      const btn = evt.currentTarget;
      const original = btn.textContent;
      btn.textContent = '✓ 복사됨';
      setTimeout(() => btn.textContent = original, 1400);
    });
  }

  // 6. 폼 제출 시 (Hidden 태그 세팅 + 로딩 스피너 동작)
  document.getElementById('contentForm').addEventListener('submit', function(e) {
    // 선택된 키워드 검증
    const keywords = [...document.querySelectorAll('#keywordGroup input:checked')];
    if (keywords.length === 0) {
      e.preventDefault(); // 제출 막기
      alert('추가 정보 키워드를 1개 이상 선택해주세요.');
      return;
    }

    // 선택된 SNS 검증
    const activeSNS = [...document.querySelectorAll('.chip.active')].map(c => c.dataset.sns);
    if (activeSNS.length === 0) {
      e.preventDefault(); // 제출 막기
      alert('게시할 SNS를 1개 이상 선택해 주세요.');
      return;
    }

    // 숨김 필드에 값 채우기 (Java Controller로 넘길 데이터)
    document.getElementById('platformsHidden').value = activeSNS.join(',');
    document.getElementById('tonesHidden').value = [...document.querySelectorAll('.tone-item.active')].map(t => t.dataset.tone).join(',');

    // 로딩 애니메이션 켜기
    document.getElementById('generateBtn').classList.add('loading');
  });