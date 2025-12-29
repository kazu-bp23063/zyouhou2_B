const loginName = /*[[${session.loginName}]]*/ 'Guest';
    // パスから /app を抜く
    const socket = new WebSocket("ws://localhost:8080/client-management");

    socket.onopen = () => {
        const msg = { taskName: "MATCHING", userName: loginName, userId: loginName };
        socket.send(JSON.stringify(msg));
    };

    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("Received:", data);

        if (data.taskName === "WAIT_STATUS") {
            updatePanels(data.players);
        } 
        else if (data.taskName === "MATCH_FOUND") {
            console.log("Success! RoomID:", data.roomId);
            if (data.roomId) {
                window.location.href = `/game?roomId=${data.roomId}&playerId=${loginName}`;
            }
        }
    };

    function updatePanels(players) {
        for (let i = 1; i <= 4; i++) {
            const slot = document.getElementById(`p${i}`);
            if (!slot) continue;

            const nameLabel = slot.querySelector('.name');
            const playerName = players[i - 1];

            if (playerName) {
                slot.classList.add('connected');
                nameLabel.innerText = playerName;
            } else {
                slot.classList.remove('connected');
                nameLabel.innerText = "待機中...";
            }
        }
    }