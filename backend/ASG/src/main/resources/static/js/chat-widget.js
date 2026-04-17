class SocialDamoaChatWidget {
    constructor() {
        this.isOpen = false;
        this.isDragging = false;
        this.dragOffset = { x: 0, y: 0 };
        
        // 우측 및 하단 기준 여백 초기값
        this.offsetFromRight = 30;
        this.offsetFromBottom = 30;

        this.stompClient = null;
        this.roomId = this.getOrCreateRoomId();
        
        // 세션 저장소에서 기존 대화 내역 로드
        this.history = JSON.parse(sessionStorage.getItem(`sd_chat_history_${this.roomId}`)) || [];
        
        this.init();
    }

    init() {
        this.createDOM();
        this.setInitialPosition();
        this.loadHistory();
        this.makeDraggable();
        this.bindEvents();
        this.connectWebSocket();
        
        // 윈도우 리사이즈 시 상대적 위치 유지를 위한 이벤트 바인딩
        window.addEventListener('resize', () => this.applyOffsetPosition());
    }

    // 1. 초기 위치 설정 및 상대적 여백 계산
    setInitialPosition() {
        const fab = document.getElementById('sd-chatbot-fab');
        const rect = fab.getBoundingClientRect();
        
        // CSS의 right/bottom 설정을 left/top 절대 좌표로 변환하여 제어권 획득
        fab.style.right = 'auto';
        fab.style.bottom = 'auto';
        fab.style.left = `${rect.left}px`;
        fab.style.top = `${rect.top}px`;

        this.updateOffsets();
    }

    // 2. 현재 위치를 바탕으로 우측/하단으로부터의 거리 저장
    updateOffsets() {
        const fab = document.getElementById('sd-chatbot-fab');
        const rect = fab.getBoundingClientRect();
        this.offsetFromRight = window.innerWidth - rect.right;
        this.offsetFromBottom = window.innerHeight - rect.bottom;
    }

    // 3. 브라우저 크기 변경 시 저장된 여백을 기준으로 좌표 재계산
    applyOffsetPosition() {
        const fab = document.getElementById('sd-chatbot-fab');
        const padding = 10;
        const fabSize = 65;

        let newLeft = window.innerWidth - this.offsetFromRight - fabSize;
        let newTop = window.innerHeight - this.offsetFromBottom - fabSize;

        // 화면 밖 이탈 방지 (Clamping)
        newLeft = Math.max(padding, Math.min(newLeft, window.innerWidth - fabSize - padding));
        newTop = Math.max(padding, Math.min(newTop, window.innerHeight - fabSize - padding));

        fab.style.left = `${newLeft}px`;
        fab.style.top = `${newTop}px`;

        if (this.isOpen) {
            this.syncWindowPosition(newLeft, newTop);
        }
    }

    getOrCreateRoomId() {
        let roomId = sessionStorage.getItem('sd_chat_room_id');
        if (!roomId) {
            roomId = 'sd_room_' + Math.random().toString(36).substr(2, 9);
            sessionStorage.setItem('sd_chat_room_id', roomId);
        }
        return roomId;
    }

	createDOM() {
	        // FAB (버튼) 생성
	        if (!document.getElementById('sd-chatbot-fab')) {
	            const fab = document.createElement('div');
	            fab.id = 'sd-chatbot-fab';
	            
	            const img = document.createElement('img');
	            img.src = '/image/chatbot2-white.png';
	            img.alt = '소셜다모아 챗봇';
	            
	            img.style.width = '110%';
	            img.style.height = '110%';
	            img.style.objectFit = 'cover';
	            img.style.display = 'block';
	            
	            // HTML5 기본 이미지 드래그 방지
	            img.style.pointerEvents = 'none';
	            img.style.userSelect = 'none';
	            img.draggable = false;

	            fab.appendChild(img);
	            document.body.appendChild(fab);
	        }

	        // 채팅창 생성
	        if (!document.getElementById('sd-chatbot-window')) {
	            const windowDiv = document.createElement('div');
	            windowDiv.id = 'sd-chatbot-window';
	            windowDiv.innerHTML = `
	                <div id="sd-chatbot-header">
	                    <span>소셜다모아 어시스턴트</span>
	                    <span id="sd-chatbot-close" style="cursor:pointer; font-size:20px;">&times;</span>
	                </div>
	                <div id="sd-messages" style="flex:1; padding:20px; overflow-y:auto; background:#f8f9fa; display:flex; flex-direction:column;"></div>
	                <div id="sd-input-area" style="padding:15px; border-top:1px solid #eee; display:flex; background:white;">
	                    <input type="text" id="sd-chat-input" style="flex:1; border:1px solid #ddd; padding:10px; border-radius:8px;" placeholder="문의 내용을 입력하세요...">
	                    <button id="sd-chat-send" style="margin-left:10px; background:#3E5480; color:white; border:none; padding:10px 15px; border-radius:8px; cursor:pointer;">전송</button>
	                </div>
	            `;
	            document.body.appendChild(windowDiv);
	        }
	    }

    makeDraggable() {
        const fab = document.getElementById('sd-chatbot-fab');
        let startX, startY;

        const onMouseMove = (e) => {
            // 5px 이상 이동 시에만 드래그 상태로 간주 (클릭과 구분)
            if (Math.abs(e.clientX - startX) > 5 || Math.abs(e.clientY - startY) > 5) {
                this.isDragging = true;
            }
            
            if (!this.isDragging) return;

            const padding = 10;
            const fabSize = 65;
            let newX = e.clientX - this.dragOffset.x;
            let newY = e.clientY - this.dragOffset.y;

            newX = Math.max(padding, Math.min(newX, window.innerWidth - fabSize - padding));
            newY = Math.max(padding, Math.min(newY, window.innerHeight - fabSize - padding));

            fab.style.left = `${newX}px`;
            fab.style.top = `${newY}px`;

            if (this.isOpen) {
                this.syncWindowPosition(newX, newY);
            }
        };

        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
            
            if (this.isDragging) {
                this.updateOffsets(); // 드래그 종료 후 우하단 기준 여백 갱신
            }
            setTimeout(() => { this.isDragging = false; }, 100);
        };

        fab.addEventListener('mousedown', (e) => {
            startX = e.clientX;
            startY = e.clientY;
            this.dragOffset.x = e.clientX - fab.offsetLeft;
            this.dragOffset.y = e.clientY - fab.offsetTop;
            document.addEventListener('mousemove', onMouseMove);
            document.addEventListener('mouseup', onMouseUp);
        });
    }

    syncWindowPosition(fabLeft, fabTop) {
        const win = document.getElementById('sd-chatbot-window');
        const winWidth = 380;
        const winHeight = 600;
        const padding = 10;

        let targetLeft = fabLeft - (winWidth - 65); 
        let targetTop = fabTop - (winHeight + 15);

        targetLeft = Math.max(padding, Math.min(targetLeft, window.innerWidth - winWidth - padding));
        targetTop = Math.max(padding, Math.min(targetTop, window.innerHeight - winHeight - padding));

        win.style.left = `${targetLeft}px`;
        win.style.top = `${targetTop}px`;
    }

    bindEvents() {
        const fab = document.getElementById('sd-chatbot-fab');
        fab.addEventListener('click', () => {
            if (!this.isDragging) this.toggleChat();
        });

        document.getElementById('sd-chatbot-close').addEventListener('click', () => this.toggleChat());
        document.getElementById('sd-chat-send').addEventListener('click', () => this.sendMessage());
        document.getElementById('sd-chat-input').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    }

    toggleChat() {
        const win = document.getElementById('sd-chatbot-window');
        const fab = document.getElementById('sd-chatbot-fab');
        this.isOpen = !this.isOpen;
        
        win.style.display = this.isOpen ? 'flex' : 'none';
        if (this.isOpen) {
            this.syncWindowPosition(parseFloat(fab.style.left), parseFloat(fab.style.top));
        }
    }

    connectWebSocket() {
        const socket = new SockJS('/ws/chatbot');
        this.stompClient = Stomp.over(socket);
        this.stompClient.connect({}, () => {
            // 기존 내역이 없을 때만 초기 환영 인사 요청
            if (this.history.length === 0) {
                this.sendInitialMenuRequest();
            }
            this.stompClient.subscribe('/topic/' + this.roomId, (res) => {
                const data = JSON.parse(res.body);
                this.saveAndAppendMessage(data.content, 'bot', data.quickReplies);
            });
        });
    }

    saveAndAppendMessage(msg, sender, quickReplies = []) {
        this.history.push({ msg, sender, quickReplies });
        sessionStorage.setItem(`sd_chat_history_${this.roomId}`, JSON.stringify(this.history));
        this.appendMessage(msg, sender, quickReplies);
    }

    loadHistory() {
        const container = document.getElementById('sd-messages');
        container.innerHTML = '';
        this.history.forEach(item => {
            this.appendMessage(item.msg, item.sender, item.quickReplies);
        });
    }

    sendInitialMenuRequest() {
        this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
            roomId: this.roomId,
            senderType: 'USER',
            content: '시작'
        }));
    }

    sendMessage() {
        const input = document.getElementById('sd-chat-input');
        const message = input.value.trim();
        if (message && this.stompClient) {
            this.saveAndAppendMessage(message, 'user');
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
                roomId: this.roomId,
                senderType: 'USER',
                content: message
            }));
            input.value = '';
        }
    }

    appendMessage(msg, sender, quickReplies = []) {
        const container = document.getElementById('sd-messages');
        const msgDiv = document.createElement('div');
        msgDiv.style.margin = '10px 0';
        msgDiv.style.textAlign = sender === 'user' ? 'right' : 'left';
        
        const bgColor = sender === 'user' ? '#e3f2fd' : '#ffffff';
        msgDiv.innerHTML = `<span style="background:${bgColor}; border: 1px solid #eee; padding:10px 15px; border-radius:15px; display:inline-block; max-width:80%; box-shadow: 0 2px 4px rgba(0,0,0,0.05); color: #333;">${msg}</span>`;
        container.appendChild(msgDiv);

        if (sender === 'bot' && quickReplies && quickReplies.length > 0) {
            const replyContainer = document.createElement('div');
            replyContainer.style.cssText = 'text-align:left; margin-top:8px; margin-bottom:15px; display:flex; flex-wrap:wrap; gap:8px;';

            quickReplies.forEach(replyText => {
                const btn = document.createElement('button');
                btn.innerText = replyText;
                btn.style.cssText = 'padding: 8px 14px; background: white; border: 1px solid #4338CA; color: #372EA6; border-radius: 20px; cursor: pointer; font-size: 13px;';
                btn.onclick = () => {
                    this.saveAndAppendMessage(replyText, 'user');
                    this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
                        roomId: this.roomId,
                        senderType: 'USER',
                        content: replyText
                    }));
                };
                replyContainer.appendChild(btn);
            });
            container.appendChild(replyContainer);
        }
        container.scrollTop = container.scrollHeight;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new SocialDamoaChatWidget();
});