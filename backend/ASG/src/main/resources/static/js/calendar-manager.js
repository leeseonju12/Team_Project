/* ==========================================================================
   calendar-manager.js
   일정 관리 전용 모듈
   - FullCalendar 초기화
   - Pending 드래그앤드롭
   - 시간 선택 모달
   - 일정 저장 API
   - 일정 미리보기 모달
   - 캘린더 확장/축소
   ========================================================================== */

window.CalendarManager = (function () {
    let deps = {};
    let draggedPendingItem = null;
    let pendingScheduleCtx = null;
    let pendingEventChangeCtx = null;
    let previewEventsBound = false;
    let calendarDropEventsBound = false;

    /* --------------------------------------------------------------------------
       0. Safe Helpers
       -------------------------------------------------------------------------- */

    /**
     * post 객체 또는 캘린더 이벤트에서 contentId를 추출한다.
     * post: contentId | id | postId 순으로 탐색
     * event: extendedProps.contentId | extendedProps.id | extendedProps.postId | event.id 순으로 탐색
     */
    function getContentId(source, isEvent = false) {
        if (!source || typeof source !== 'object') return null;

        const candidates = isEvent
            ? [
                source.extendedProps?.contentId,
                source.extendedProps?.id,
                source.extendedProps?.postId,
                source.id
              ]
            : [source.contentId, source.id, source.postId];

        for (const value of candidates) {
            if (value !== undefined && value !== null && String(value).trim() !== '') {
                return value;
            }
        }
        return null;
    }

    function hasValidContentId(post) {
        return getContentId(post) !== null;
    }

    function findPendingPostByAnyId(rawId) {
        if (rawId == null) return null;
        return (deps.state?.pendingPosts || []).find((p) =>
            String(getContentId(p)) === String(rawId)
        ) || null;
    }

    function getPostLabel(post) {
		if (!post) return '제목 없는 포스트';

		    // DB 스키마인 menu_name을 최우선으로 탐색
		    return post.menu_name || 
		           post.menuName || 
		           post.title || 
		           post.label || 
		           (post.content ? post.content.substring(0, 15) + '...' : '내용 없음');
    }

    function safeToast(message) {
        deps.uiManager?.showToast?.(message);
    }

    /* --------------------------------------------------------------------------
       1. Init
       -------------------------------------------------------------------------- */
    function init(options) {
        deps = {
            state: options.state,
            uiManager: options.uiManager,
            PLATFORM_CONFIG: options.PLATFORM_CONFIG,
            PASTEL_PALETTE: options.PASTEL_PALETTE,
            renderPendingHTML: options.renderPendingHTML,
            publishPost: options.publishPost
        };

        exposeGlobals();
        bindPreviewModalEvents();

    }

    function exposeGlobals() {
        window.initCalendar = initCalendar; // [FIX] 외부 재초기화용으로 노출
        window.toggleCalendarExpand = toggleCalendarExpand;
        window.closePreviewModal = closePreviewModal;
        window.cancelTimeSelection = cancelTimeSelection;
        window.confirmTimeSelection = confirmTimeSelection;
        window.onPendingDragStart = onPendingDragStart;
        window.onPendingDragEnd = onPendingDragEnd;
    }

    /* --------------------------------------------------------------------------
       2. Calendar
       -------------------------------------------------------------------------- */
    function initCalendar() {
        const calEl = document.getElementById('fullcalendar');
        if (!calEl || typeof FullCalendar === 'undefined') return;

        // 기존 인스턴스가 있으면 제거 후 재생성
        if (deps.state.calendarInstance) {
            deps.state.calendarInstance.destroy();
            deps.state.calendarInstance = null;
            calendarDropEventsBound = false; // [FIX] 재초기화 시 드롭존 재바인딩 허용
        }

        deps.state.calendarInstance = new FullCalendar.Calendar(calEl, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            headerToolbar: { left: 'prev', center: 'title', right: 'next' },
            height: 'auto',
            editable: true,
            droppable: true,
            events: '/api/posts/events',
            eventDrop: onCalendarEventDrop,
            eventClick: (info) => {
                info.jsEvent.preventDefault();
                openPreviewModal(info.event);
            }
        });

        deps.state.calendarInstance.render();
        setupCalendarDropZones();
    }

    function onCalendarEventDrop(info) {
        try {
            const newStart = info.event.start;
            if (!newStart) {
                info.revert();
                safeToast('변경할 일정 시간이 없습니다.');
                return;
            }

            const contentId = getContentId(info.event, true);
            if (contentId === null) {
                console.error('[onCalendarEventDrop] contentId not found:', info.event);
                info.revert();
                safeToast('일정의 게시물 ID를 찾을 수 없습니다.');
                return;
            }

            const dateStr = `${newStart.getFullYear()}-${pad2(newStart.getMonth() + 1)}-${pad2(newStart.getDate())}`;

            if (isPastDateOnly(dateStr)) {
                info.revert();
                safeToast('지난 날짜로는 일정을 변경할 수 없습니다.');
                return;
            }

            openExistingEventTimeModal(info, dateStr);
        } catch (err) {
            console.error('[CalendarManager] 일정 이동 처리 실패:', err);
            info.revert();
            safeToast(`일정 변경 실패: ${err.message}`);
        }
    }

    function refreshCalendarLayoutSize() {
        try {
            deps.state.calendarInstance?.updateSize();
        } catch (e) {
            // noop
        }
    }

    function toggleCalendarExpand() {
        const grid = document.getElementById('dashboardGrid');
        const btn = document.getElementById('calendarExpandBtn');
        if (!grid) return;

        const isExpanding = grid.classList.toggle('calendar-expanded');

        if (btn) {
            btn.setAttribute('aria-expanded', String(isExpanding));
            const label = btn.querySelector('.btn-calendar-expand-label');
            if (label) label.textContent = isExpanding ? '축소' : '확장';
        }

        const refresh = () => refreshCalendarLayoutSize();
        requestAnimationFrame(refresh);

        let settled = false;
        const onTransitionEnd = (ev) => {
            if (ev.target === grid && /grid-template-columns/i.test(ev.propertyName)) {
                if (!settled) {
                    settled = true;
                    refresh();
                    requestAnimationFrame(refresh);
                }
                grid.removeEventListener('transitionend', onTransitionEnd);
            }
        };

        grid.addEventListener('transitionend', onTransitionEnd);

        setTimeout(() => {
            if (!settled) {
                settled = true;
                refresh();
                grid.removeEventListener('transitionend', onTransitionEnd);
            }
        }, 550);
    }

    /* --------------------------------------------------------------------------
       3. Date/Time Utils
       -------------------------------------------------------------------------- */
    function pad2(num) {
        return String(num).padStart(2, '0');
    }

    function toLocalDateTimeString(date) {
        if (!(date instanceof Date) || Number.isNaN(date.getTime())) return null;
        return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}T${pad2(date.getHours())}:${pad2(date.getMinutes())}:${pad2(date.getSeconds())}`;
    }

    function toDateOnly(date) {
        return new Date(date.getFullYear(), date.getMonth(), date.getDate());
    }

    function parseDateOnly(dateStr) {
        const [y, m, d] = String(dateStr).split('-').map(Number);
        return new Date(y, m - 1, d);
    }

    function formatHHMM(date) {
        return `${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
    }

    function isPastDateOnly(dateStr) {
        return toDateOnly(parseDateOnly(dateStr)).getTime() < toDateOnly(new Date()).getTime();
    }

    function getMinSelectableTime(dateStr) {
        const now = new Date();
        const target = parseDateOnly(dateStr);

        if (toDateOnly(now).getTime() !== toDateOnly(target).getTime()) return '00:00';

        const rounded = new Date(now);
        if (rounded.getSeconds() > 0 || rounded.getMilliseconds() > 0) {
            rounded.setMinutes(rounded.getMinutes() + 1);
        }
        rounded.setSeconds(0, 0);
        return formatHHMM(rounded);
    }

    /* --------------------------------------------------------------------------
       4. Pending Drag & Drop
       -------------------------------------------------------------------------- */
    function onPendingDragStart(event, id) {
        draggedPendingItem = findPendingPostByAnyId(id);

        if (!draggedPendingItem || !hasValidContentId(draggedPendingItem)) {
            safeToast('드래그할 게시물 정보를 찾을 수 없습니다.');
            draggedPendingItem = null;
            return;
        }

        const resolvedContentId = getContentId(draggedPendingItem);
        event.dataTransfer.effectAllowed = 'move';
        event.dataTransfer.setData('text/plain', String(resolvedContentId));
        event.currentTarget?.classList.add('dragging');
    }

    function onPendingDragEnd(event) {
        event.currentTarget?.classList.remove('dragging');
        document.querySelectorAll('.pending-item.dragging')
            .forEach((el) => el.classList.remove('dragging'));
        document.querySelectorAll('.fc-day.drag-over, .fc-daygrid-day.drag-over, [data-date].drag-over')
            .forEach((el) => el.classList.remove('drag-over'));
    }

    function setupCalendarDropZones() {
        if (calendarDropEventsBound) return;
        calendarDropEventsBound = true;

        const calEl = document.getElementById('fullcalendar');
        if (!calEl) return;

        calEl.addEventListener('dragover', (e) => {
            e.preventDefault();
            const dayCell = e.target.closest('.fc-daygrid-day, .fc-day, [data-date]');
            if (!dayCell) return;
            document.querySelectorAll('.drag-over').forEach((el) => el.classList.remove('drag-over'));
            dayCell.classList.add('drag-over');
        });

        calEl.addEventListener('dragleave', (e) => {
            e.target.closest('.fc-daygrid-day, .fc-day, [data-date]')?.classList.remove('drag-over');
        });

        calEl.addEventListener('drop', (e) => {
            e.preventDefault();
            document.querySelectorAll('.drag-over').forEach((el) => el.classList.remove('drag-over'));

            const dayCell =
                e.target.closest('.fc-daygrid-day') ||
                e.target.closest('.fc-day') ||
                e.target.closest('[data-date]');

            const dateStr = dayCell?.getAttribute('data-date') || dayCell?.dataset?.date;

            if (!dateStr) { safeToast('아이템을 정확한 날짜 칸에 놓아주세요.'); return; }
            if (!draggedPendingItem || !hasValidContentId(draggedPendingItem)) {
                safeToast('드래그된 아이템 정보를 찾을 수 없습니다.');
                draggedPendingItem = null;
                return;
            }
            if (isPastDateOnly(dateStr)) {
                safeToast('지난 날짜에는 일정을 등록할 수 없습니다.');
                draggedPendingItem = null;
                return;
            }

            openTimeModal(draggedPendingItem, dateStr);
        });
    }

    /* --------------------------------------------------------------------------
       5. Time Selection Modal
       -------------------------------------------------------------------------- */
    function openTimeModal(post, dateStr) {
        const contentId = getContentId(post);
        if (contentId === null) {
            safeToast('게시물 ID를 찾을 수 없습니다.');
            return;
        }

        pendingScheduleCtx = { post: { ...post, contentId }, dateStr };

        const modal = document.getElementById('timeSelectionModal');
        if (!modal) {
            saveSchedule(pendingScheduleCtx.post, `${dateStr}T12:00:00`);
            return;
        }

        const dateDisplay = document.getElementById('selectedDateDisplay');
        const timeInput = document.getElementById('scheduledTime');

        if (dateDisplay) dateDisplay.textContent = `${dateStr} 날짜의 게시 시간을 설정합니다.`;

        if (timeInput) {
            const minTime = getMinSelectableTime(dateStr);
            timeInput.min = minTime;
            timeInput.value = minTime;
        }

        modal.classList.remove('hidden');
        modal.style.display = 'flex';
    }

    function openExistingEventTimeModal(info, dateStr) {
        const event = info.event;
        const contentId = getContentId(event, true);

        if (contentId === null) {
            info.revert();
            safeToast('일정의 게시물 ID를 찾을 수 없습니다.');
            return;
        }

        // [REFACTOR] info.event와 event는 동일 참조이므로 event만 보관
        pendingEventChangeCtx = { info, contentId, dateStr };

        const modal = document.getElementById('timeSelectionModal');
        if (!modal) { info.revert(); safeToast('시간 선택 모달을 찾을 수 없습니다.'); return; }

        const dateDisplay = document.getElementById('selectedDateDisplay');
        const timeInput = document.getElementById('scheduledTime');

        if (dateDisplay) dateDisplay.textContent = `${dateStr} 날짜의 변경 시간을 설정합니다.`;

        if (timeInput) {
            const current = event.start || new Date();
            const currentTime = formatHHMM(current);
            const minTime = getMinSelectableTime(dateStr);
            timeInput.min = minTime;
            timeInput.value = currentTime < minTime ? minTime : currentTime;
        }

        modal.classList.remove('hidden');
        modal.style.display = 'flex';
    }

    function cancelTimeSelection() {
        const modal = document.getElementById('timeSelectionModal');
        if (modal) {
            modal.classList.add('hidden');
            modal.style.display = 'none';
        }

        if (typeof pendingEventChangeCtx?.info?.revert === 'function') {
            pendingEventChangeCtx.info.revert();
        }

        pendingScheduleCtx = null;
        pendingEventChangeCtx = null;
        draggedPendingItem = null;
    }

    function hideTimeModal() {
        const modal = document.getElementById('timeSelectionModal');
        if (modal) { modal.classList.add('hidden'); modal.style.display = 'none'; }
    }

    async function confirmTimeSelection() {
        const timeInput = document.getElementById('scheduledTime');
        const timeVal = timeInput?.value;
        if (!timeVal) { safeToast('시간을 선택해주세요.'); return; }

        // 신규 pending 일정 등록
        if (pendingScheduleCtx) {
            const contentId = getContentId(pendingScheduleCtx.post);
            if (contentId === null) {
                safeToast('게시물 ID를 찾을 수 없습니다.');
                return;
            }

            const fullDT = `${pendingScheduleCtx.dateStr}T${timeVal}:00`;
            if (new Date(fullDT).getTime() < Date.now()) {
                safeToast('현재 시각 이후 시간으로 설정해주세요.');
                return;
            }

            await saveSchedule({ ...pendingScheduleCtx.post, contentId }, fullDT);
            hideTimeModal();
            pendingScheduleCtx = null;
            draggedPendingItem = null;
            return;
        }

        // 기존 캘린더 일정 변경
        if (pendingEventChangeCtx) {
            const { info, dateStr } = pendingEventChangeCtx;
            const event = info.event; // [REFACTOR] ctx에서 info.event로 직접 접근

            const fullDT = `${dateStr}T${timeVal}:00`;
            const selected = new Date(fullDT);
            if (selected.getTime() < Date.now()) {
                safeToast('현재 시각 이후 시간으로 설정해주세요.');
                return;
            }

            try {
                event.setStart(selected);
                await updateScheduledEvent(event);
                hideTimeModal();
                safeToast(`📅 일정이 ${dateStr} ${timeVal}(으)로 변경되었습니다.`);
                deps.state.calendarInstance?.refetchEvents();
            } catch (err) {
                console.error('[CalendarManager] 일정 변경 실패:', err);
                if (typeof info.revert === 'function') info.revert();
                safeToast(`일정 변경 실패: ${err.message}`);
            } finally {
                pendingEventChangeCtx = null;
            }
            return;
        }

        safeToast('예약할 게시물 정보가 없습니다.');
    }

    async function saveSchedule(post, scheduledDate) {
        try {
            const contentId = getContentId(post);
            if (contentId === null) throw new Error('게시물 ID가 없어 일정을 저장할 수 없습니다.');

            const res = await fetch('/api/posts/schedule', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ contentId, scheduledDate })
            });

            const text = await res.text();
            if (!res.ok) {
                let message = text;
                try { message = JSON.parse(text)?.message || JSON.parse(text)?.error || text; } catch (e) {}
                throw new Error(`HTTP ${res.status} - ${message}`);
            }

            removePending(contentId);
            deps.state.calendarInstance?.refetchEvents();
            safeToast(`📅 ${getPostLabel(post)} 포스트가 ${scheduledDate.slice(0, 10)}에 저장되었습니다.`);
        } catch (err) {
            console.error('[CalendarManager] 일정 저장 실패:', err);
            safeToast(`저장 실패: ${err.message}`);
        }
    }

    async function updateScheduledEvent(event) {
        const contentId = getContentId(event, true);
        const scheduledDate = toLocalDateTimeString(event?.start);

        if (contentId === null) throw new Error('캘린더 이벤트의 contentId를 찾을 수 없습니다.');
        if (!scheduledDate) throw new Error('변경할 일정 시간이 올바르지 않습니다.');

        const res = await fetch('/api/posts/schedule', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ contentId, scheduledDate })
        });

        const text = await res.text();
        if (!res.ok) {
            let message = text;
            try { message = JSON.parse(text)?.message || JSON.parse(text)?.error || text; } catch (e) {}
            throw new Error(`HTTP ${res.status} - ${message}`);
        }

        return true;
    }

    function removePending(id) {
        deps.state.pendingPosts = (deps.state.pendingPosts || []).filter((p) => {
            const cid = getContentId(p);
            return cid === null || String(cid) !== String(id);
        });
        deps.renderPendingHTML?.();
    }

    /* --------------------------------------------------------------------------
       6. Preview Modal - Platform Resolve
       -------------------------------------------------------------------------- */

    // [REFACTOR] 배열 includes 반복 → Map으로 교체 (O(1) 룩업)
    const SNS_ALIAS_MAP = new Map([
        ['instagram', 'instagram'], ['insta', 'instagram'], ['ig', 'instagram'],
        ['facebook', 'facebook'],   ['fb', 'facebook'],
        ['naver', 'naver'],         ['naverblog', 'naver'], ['naver_blog', 'naver'], ['blog', 'naver'],
        ['kakao', 'kakao'],         ['kakaochannel', 'kakao'], ['kakao_channel', 'kakao'],
                                    ['kakaotalk', 'kakao'], ['kk', 'kakao'],
        ['community', 'community'], ['comm', 'community']
    ]);

    function normalizeSns(value) {
        if (!value) return '';
        return SNS_ALIAS_MAP.get(String(value).trim().toLowerCase()) || '';
    }

    function resolveEventSns(event) {
        const classNames = (event.classNames || []).join(' ').toLowerCase();

        const fromExtended =
            normalizeSns(event.extendedProps?.sns) ||
            normalizeSns(event.extendedProps?.platform) ||
            normalizeSns(event.extendedProps?.channel) ||
            normalizeSns(event.extendedProps?.type);

        if (fromExtended) return fromExtended;

        if (classNames.includes('fc-event-ig'))   return 'instagram';
        if (classNames.includes('fc-event-fb'))   return 'facebook';
        if (classNames.includes('fc-event-nv'))   return 'naver';
        if (classNames.includes('fc-event-kk'))   return 'kakao';
        if (classNames.includes('fc-event-comm')) return 'community';

        const border = String(event.borderColor || '').toLowerCase();
        const bg     = String(event.backgroundColor || '').toLowerCase();

        if (border === '#e1306c' || bg === '#e1306c') return 'instagram';
        if (border === '#1877f2' || bg === '#1877f2') return 'facebook';
        if (border === '#03c75a' || bg === '#03c75a') return 'naver';
        if (border === '#fee500' || bg === '#fee500') return 'kakao';
        if (border === '#6366f1' || bg === '#6366f1') return 'community';

        return 'community';
    }

    /* --------------------------------------------------------------------------
       7. Preview Modal
       -------------------------------------------------------------------------- */
    function openPreviewModal(event) {
        const modal = document.getElementById('previewModal');
        const shell = document.getElementById('pmShell');
        if (!modal) return;

        const sns = resolveEventSns(event);
        const platform = deps.PLATFORM_CONFIG[sns] || { label: sns, color: '#6366F1' };

        const pillDot   = document.getElementById('pmPillDot');
        const pillLabel = document.getElementById('pmPlatformLabel');
        const timeText  = document.getElementById('pmTimeText');

        if (pillDot)   pillDot.style.background = platform.color;
        if (pillLabel) pillLabel.textContent = platform.label;

        if (timeText) {
            const startDate = event.start ? new Date(event.start) : null;
            if (startDate) {
                const datePart = startDate.toLocaleDateString('ko-KR', {
                    month: 'long', day: 'numeric', weekday: 'short'
                });
                const timePart = event.startStr?.includes('T')
                    ? startDate.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false })
                    : null;
                timeText.textContent = timePart ? `${datePart} ${timePart}` : datePart;
            } else {
                timeText.textContent = '--';
            }
        }

        const savedContent = deps.state.generatedContent[sns];
        const bodyText =
            savedContent?.text ||
            event.extendedProps?.bodyText ||
            event.extendedProps?.content ||
            event.title || '';

        const menuName = event.title || '';

        document.querySelectorAll('[id^="pmBody"]').forEach((el) => (el.style.display = 'none'));

        const renderers = {
            instagram: renderInstagramPreview,
            facebook:  renderFacebookPreview,
            naver:     renderNaverPreview,
            kakao:     renderKakaoPreview
        };
        (renderers[sns] || renderCommunityPreview)(event, bodyText, menuName);

        bindPreviewFooterButtons(event, sns);

        if (shell) shell.classList.remove('closing');
        modal.classList.remove('hidden');
    }

    function renderInstagramPreview(event, bodyText) {
        const body = document.getElementById('pmBodyInstagram');
        if (!body) return;
        body.style.display = 'block';

        const captionEl = document.getElementById('igCaptionText');
        if (captionEl) captionEl.textContent = bodyText;

        const tsEl = document.getElementById('igTimestamp');
        if (tsEl) {
            tsEl.textContent = event.start
                ? new Date(event.start).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
                : '';
        }

        const img = document.getElementById('igPhotoImg');
        const ph  = document.getElementById('igPhotoPlaceholder');
        if (img && ph) {
            const hasImg = deps.state.uploadedImages.length > 0;
            img.src          = hasImg ? deps.state.uploadedImages[0] : '';
            img.style.display = hasImg ? 'block' : 'none';
            ph.style.display  = hasImg ? 'none'  : 'block';
        }

        const htRow = document.getElementById('igHashtagRow');
        if (htRow) {
            const tags = bodyText.match(/#[\w가-힣]+/g) || [];
            htRow.innerHTML = tags.map((t) => `<span class="ig-hashtag-chip">${t}</span>`).join('');
        }
    }

    function renderFacebookPreview(event, bodyText) {
        const body = document.getElementById('pmBodyFacebook');
        if (!body) return;
        body.style.display = 'block';

        const textEl = document.getElementById('fbsText');
        if (textEl) textEl.textContent = bodyText;

        const timeEl = document.getElementById('fbsTime');
        if (timeEl) {
            timeEl.textContent = event.start
                ? new Date(event.start).toLocaleDateString('ko-KR')
                : '방금 전';
        }

        const imgWrap = document.getElementById('fbsMediaWrap');
        if (imgWrap) {
            imgWrap.innerHTML = deps.state.uploadedImages.length > 0
                ? `<div class="fb-s-img-wrap"><img class="fb-s-img" src="${deps.state.uploadedImages[0]}" alt=""></div>`
                : '';
        }
    }

    function renderNaverPreview(event, bodyText, menuName) {
        const body = document.getElementById('pmBodyBlog');
        if (!body) return;
        body.style.display = 'block';

        const titleEl   = document.getElementById('blogArtTitle');
        const artBody   = document.getElementById('blogArtBody');
        const metaDate  = document.getElementById('blogMetaDate');

        if (titleEl)  titleEl.textContent  = menuName || '포스트 미리보기';
        if (artBody)  artBody.textContent  = bodyText;
        if (metaDate) metaDate.textContent = event.start ? new Date(event.start).toLocaleDateString('ko-KR') : '';

        const img = document.getElementById('blogArtImg');
        const ph  = document.getElementById('blogArtImgPlaceholder');
        if (img && ph) {
            const hasImg = deps.state.uploadedImages.length > 0;
            img.src          = hasImg ? deps.state.uploadedImages[0] : '';
            img.style.display = hasImg ? 'block' : 'none';
            ph.style.display  = hasImg ? 'none'  : 'block';
        }
    }

    function renderKakaoPreview(event, bodyText, menuName) {
        const body = document.getElementById('pmBodyKakao');
        if (!body) return;
        body.style.display = 'flex';

        const textEl      = document.getElementById('kksText');
        const dateEl      = document.getElementById('kksDate');
        const linkTitleEl = document.getElementById('kksLinkTitle');
        const mediaSection = document.getElementById('kksMediaSection');

        if (textEl)      textEl.textContent      = bodyText;
        if (dateEl)      dateEl.textContent       = event.start ? new Date(event.start).toLocaleDateString('ko-KR') : '';
        if (linkTitleEl) linkTitleEl.textContent  = menuName || '채널에서 자세히 보기';

        if (mediaSection) {
            mediaSection.innerHTML = deps.state.uploadedImages.length > 0
                ? `<div class="kk-s-media"><img class="kk-s-img" src="${deps.state.uploadedImages[0]}" alt=""></div>`
                : `<div class="kk-s-no-media"><div class="kk-s-no-media-text">${menuName}</div></div>`;
        }
    }

    function renderCommunityPreview(event, bodyText, menuName) {
        const body = document.getElementById('pmBodyComm');
        if (!body) return;
        body.style.display = 'block';

        const textEl  = document.getElementById('commPostBody');
        const titleEl = document.getElementById('commPostTitle');
        const dateEl  = document.getElementById('commPostDate');

        if (textEl)  textEl.textContent  = bodyText;
        if (titleEl) titleEl.textContent = menuName;
        if (dateEl)  dateEl.textContent  = event.start ? new Date(event.start).toLocaleDateString('ko-KR') : '';
    }

    function bindPreviewFooterButtons(event, sns) {
        const deleteBtn  = document.getElementById('pmBtnDelete');
        const publishBtn = document.getElementById('pmBtnPublish');

        if (deleteBtn) {
            deleteBtn.onclick = () => {
                event.remove();
                closePreviewModal();
                safeToast('🗑 일정이 삭제되었습니다.');
            };
        }

        if (publishBtn) {
            publishBtn.onclick = () => {
                closePreviewModal();
                deps.publishPost?.(sns);
            };
        }
    }

    function closePreviewModal() {
        const modal = document.getElementById('previewModal');
        const shell = document.getElementById('pmShell');
        if (!modal) return;

        if (shell) {
            shell.classList.add('closing');
            let done = false;
            const finish = () => {
                if (done) return;
                done = true;
                modal.classList.add('hidden');
                shell.classList.remove('closing');
            };
            shell.addEventListener('animationend', finish, { once: true });
            setTimeout(finish, 250);
        } else {
            modal.classList.add('hidden');
        }
    }

    function bindPreviewModalEvents() {
        if (previewEventsBound) return;
        previewEventsBound = true;

        document.getElementById('pmCloseBtn')?.addEventListener('click', closePreviewModal);
        document.getElementById('pmBackdrop')?.addEventListener('click', closePreviewModal);
        document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closePreviewModal(); });
    }

    /* --------------------------------------------------------------------------
       8. Public API
       -------------------------------------------------------------------------- */
    return {
        init,
        initCalendar,
        removePending,
        refreshCalendarLayoutSize
    };
})();