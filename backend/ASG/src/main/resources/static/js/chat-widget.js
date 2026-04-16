class SocialDamoaChatWidget {
    constructor() {
        this.isOpen = false;
        this.isDragging = false;
        this.dragOffset = { x: 0, y: 0 };
        this.stompClient = null;
        this.roomId = this.getOrCreateRoomId();
        this.init();
    }

    init() {
        this.createDOM();
        this.makeDraggable();
        this.bindEvents();
        this.connectWebSocket();
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
        const fab = document.createElement('div');
        fab.id = 'sd-chatbot-fab';
        fab.innerHTML = 'SD 톡';
        document.body.appendChild(fab);

        const windowDiv = document.createElement('div');
        windowDiv.id = 'sd-chatbot-window';
        windowDiv.innerHTML = `
            <div id="sd-chatbot-header">
                <span>소셜다모아 어시스턴트</span>
                <span id="sd-chatbot-close" style="cursor:pointer; font-size:20px;">&times;</span>
            </div>
            <div id="sd-messages" style="flex:1; padding:20px; overflow-y:auto; background:#f8f9fa;"></div>
            <div id="sd-input-area" style="padding:15px; border-top:1px solid #eee; display:flex; background:white;">
                <input type="text" id="sd-chat-input" style="flex:1; border:1px solid #ddd; padding:10px; border-radius:8px;" placeholder="문의 내용을 입력하세요...">
                <button id="sd-chat-send" style="margin-left:10px; background:#1A73E8; color:white; border:none; padding:10px 15px; border-radius:8px; cursor:pointer;">전송</button>
            </div>
        `;
        document.body.appendChild(windowDiv);
    }

    makeDraggable() {
        const fab = document.getElementById('sd-chatbot-fab');

		const onMouseMove = (e) => {
		    this.isDragging = true;
		    
		    const fabWidth = 65;
		    const fabHeight = 65;
		    const padding = 10;

		    // 버튼의 잠재적 위치 계산
		    let newX = e.clientX - this.dragOffset.x;
		    let newY = e.clientY - this.dragOffset.y;

		    // 버튼이 화면 밖으로 나가지 않도록 제한
		    newX = Math.max(padding, Math.min(newX, window.innerWidth - fabWidth - padding));
		    newY = Math.max(padding, Math.min(newY, window.innerHeight - fabHeight - padding));

		    const fab = document.getElementById('sd-chatbot-fab');
		    fab.style.left = `${newX}px`;
		    fab.style.top = `${newY}px`;

		    if (this.isOpen) {
		        this.syncWindowPosition(newX, newY);
		    }
		};

        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
            setTimeout(() => { this.isDragging = false; }, 50);
        };

        fab.addEventListener('mousedown', (e) => {
            this.dragOffset.x = e.clientX - fab.offsetLeft;
            this.dragOffset.y = e.clientY - fab.offsetTop;
            document.addEventListener('mousemove', onMouseMove);
            document.addEventListener('mouseup', onMouseUp);
        });
    }

	syncWindowPosition(fabLeft, fabTop) {
	    const win = document.getElementById('sd-chatbot-window');
	    const winWidth = 380;  // CSS에 정의된 너비
	    const winHeight = 600; // CSS에 정의된 높이
	    const padding = 10;    // 화면 가장자리 여백

	    // 1. 기본 목표 위치 계산 (버튼 좌측 상단)
	    let targetLeft = fabLeft - (winWidth - 60); 
	    let targetTop = fabTop - (winHeight + 10);

	    // 2. 가로 경계 제한 (Viewport Width 기준)
	    // 좌측 벽(padding)보다 작아지지 않게, 우측 벽(window.innerWidth - 너비)보다 커지지 않게 제한
	    const maxLeft = window.innerWidth - winWidth - padding;
	    targetLeft = Math.max(padding, Math.min(targetLeft, maxLeft));

	    // 3. 세로 경계 제한 (Viewport Height 기준)
	    // 상단 벽(padding)보다 작아지지 않게, 하단 벽(window.innerHeight - 높이)보다 커지지 않게 제한
	    const maxTop = window.innerHeight - winHeight - padding;
	    targetTop = Math.max(padding, Math.min(targetTop, maxTop));

	    // 4. 최종 좌표 적용
	    win.style.left = `${targetLeft}px`;
	    win.style.top = `${targetTop}px`;
	    win.style.right = 'auto';
	    win.style.bottom = 'auto';
	}

    bindEvents() {
        document.getElementById('sd-chatbot-fab').addEventListener('click', () => {
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
        if (this.isOpen) this.syncWindowPosition(fab.offsetLeft, fab.offsetTop);
    }

	connectWebSocket() {
	    const socket = new SockJS('/ws/chatbot');
	    this.stompClient = Stomp.over(socket);
	    this.stompClient.connect({}, () => {
	        /* 접속 성공 시 초기 메뉴를 요청하여 챗봇 시나리오 시작 */
	        this.sendInitialMenuRequest();

	        this.stompClient.subscribe('/topic/' + this.roomId, (res) => {
	            const data = JSON.parse(res.body);
	            /* 봇의 메시지와 선택지 버튼 배열을 함께 렌더링 함수로 전달 */
	            this.appendMessage(data.content, 'bot', data.quickReplies);
	        });
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
            this.appendMessage(message, 'user');
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

	    /* 1. 기본 메시지 말풍선 렌더링 */
	    const msgDiv = document.createElement('div');
	    msgDiv.style.margin = '10px 0';
	    msgDiv.style.textAlign = sender === 'user' ? 'right' : 'left';
	    msgDiv.innerHTML = `<span style="background:${sender === 'user' ? '#e3f2fd' : '#ffffff'}; border: 1px solid #eee; padding:10px 15px; border-radius:15px; display:inline-block; max-width:80%; box-shadow: 0 2px 4px rgba(0,0,0,0.05); line-height: 1.4;">${msg}</span>`;
	    container.appendChild(msgDiv);

	    /* 2. 퀵 리플라이 버튼 렌더링 (봇 메시지이며 버튼 배열이 존재하는 경우) */
	    if (sender === 'bot' && quickReplies && quickReplies.length > 0) {
	        const replyContainer = document.createElement('div');
	        replyContainer.style.textAlign = 'left';
	        replyContainer.style.marginTop = '8px';
	        replyContainer.style.marginBottom = '15px';
	        replyContainer.style.display = 'flex';
	        replyContainer.style.flexWrap = 'wrap';
	        replyContainer.style.gap = '8px';

	        quickReplies.forEach(replyText => {
	            const btn = document.createElement('button');
	            btn.innerText = replyText;
	            btn.style.cssText = 'padding: 8px 14px; background: white; border: 1px solid #1A73E8; color: #1A73E8; border-radius: 20px; cursor: pointer; font-size: 13px; transition: background 0.2s;';

	            /* 호버 효과 */
	            btn.onmouseover = () => { btn.style.background = '#e3f2fd'; };
	            btn.onmouseout = () => { btn.style.background = 'white'; };

	            /* 버튼 클릭 이벤트: 클릭한 텍스트를 메시지로 전송 */
	            btn.onclick = () => {
	                this.appendMessage(replyText, 'user');
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

