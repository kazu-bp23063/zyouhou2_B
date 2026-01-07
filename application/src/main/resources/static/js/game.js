// game.js

// 1. URLパラメータから情報を取得
const urlParams = new URLSearchParams(window.location.search);
const myPlayerId = urlParams.get('playerId');
const roomId = urlParams.get('roomId');
const statusBtn = document.getElementById('status');
const modal = document.getElementById('statusModal');
const closeBtn = document.getElementById('closeModal');

// 2. WebSocket接続
const socket = new WebSocket("ws://localhost:8080/game-server");

// 全プレイヤー情報とアイテムの状態管理
let allPlayers = []; // 全員のインデックス特定用に保持
let selectedItemType = null;
let selectedTargetValue = null;

// （三平）プレイヤーID -> 表示インデックス を保持（重なり防止のため）
const playerIndexMap = new Map(); // （三平）

document.addEventListener('DOMContentLoaded', async () => {
    const diceBtn = document.getElementById('diceStart');
    const eventMsg = document.getElementById('event-message');
    const earnedUnitsDisplay = document.getElementById('earned-units');
    const expectedUnitsDisplay = document.getElementById('expected-units');

    // --- 3. WebSocket 接続完了時の処理 ---
    socket.onopen = () => {
        const joinMsg = { taskName: "GAME_JOIN", roomId: roomId, playerId: myPlayerId };
        socket.send(JSON.stringify(joinMsg));
    };

    // --- 4. サーバーからの更新通知を処理 ---
    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("--- 受信データ ---", data);

        if (data.taskName === "GAME_UPDATE") {
            // ① 出目の表示
            const diceResult = document.querySelector('#dice-result-text span');
            if (diceResult) diceResult.innerText = data.diceValue;

            const pIndex = 0;
            // ② 駒の移動を実行
            updatePieceVisual(data.lastPlayerId, data.newPosition,pIndex);

            // ③ 単位の更新（自分の番が終わった時、または誰かが動いた時）
            if (data.lastPlayerId === myPlayerId) {
                if (earnedUnitsDisplay) earnedUnitsDisplay.innerText = data.earnedUnits;
                if (expectedUnitsDisplay) expectedUnitsDisplay.innerText = data.expectedUnits;
                document.getElementById('modal-earned').innerText = data.earnedUnits;
            }

            // ⑤ ターンの強調表示（activeクラスの付け替え）
            document.querySelectorAll('.player-status-card').forEach(c => c.classList.remove('active'));
            const nextCard = document.getElementById(`status-card-${data.nextPlayerId}`);
            if (nextCard) nextCard.classList.add('active');

            handleTurnChange(data.nextPlayerId);

            // 卒業判定
            if (data.isGraduated) {
                setTimeout(() => {
                    alert(`${data.lastPlayerId} さんが卒業しました！`);
                    location.href = `/result?roomId=${roomId}`;
                }, 800);
            }
        }
    };

    // --- 5. ダイスを振る処理 ---
    diceBtn.addEventListener('click', () => {
        diceBtn.disabled = true;
        
        const rollMsg = {
            taskName: "GAME_ROLL",
            roomId: roomId,
            playerId: myPlayerId,
            itemType: selectedItemType,
            targetValue: selectedTargetValue
        };
        socket.send(JSON.stringify(rollMsg));
        
        // アイテム使用後はリセット
        selectedItemType = null;
        selectedTargetValue = null;
        document.getElementById('selected-item-display').innerText = "使用予定: なし";
    });

    // --- 6. 初期化：現在の部屋の状態を取得 ---
    try {
        const res = await fetch(`/api/matching/status?roomId=${roomId}`);
        const room = await res.json();
        setupPlayersUI(room.players);
        
        // 最初の手番プレイヤーを確認してボタン状態をセット
        const initialTurnPlayer = room.players[room.turnIndex];
        handleTurnChange(initialTurnPlayer.id);
    } catch (err) {
        console.error("初期データの取得に失敗:", err);
    }

    statusBtn.onclick = () => modal.style.display = "block";
    closeBtn.onclick = () => modal.style.display = "none";
});

function setupPlayersUI(players) {
    allPlayers = players; 
    const container = document.querySelector('.board-container');
    const board = document.getElementById('player-status-board'); // UIボードの取得
    
    if (board) board.innerHTML = '';

    players.forEach((p, i) => {
        let piece = document.getElementById(`player-${p.id}`);
        if (!piece) {
            piece = document.createElement('div');
            piece.id = `player-${p.id}`;
            piece.className = 'player-piece';
            container.appendChild(piece);
        }
        piece.style.backgroundColor = p.color;
        piece.innerText = p.name.substring(0, 1);
        updatePieceVisual(p.id, p.currentPosition, i);

        // --- ステータスカードの生成 ---
        if (board) {
            const card = document.createElement('div');
            card.id = `status-card-${p.id}`;
            card.className = 'player-status-card';
            card.style.borderLeft = `5px solid ${p.color}`;
            card.innerHTML = `
                <div class="status-name">${p.name}</div>
                <div class="status-units">累計: <span id="card-earned-${p.id}">${p.earnedUnits}</span></div>
                <div class="status-units">次: <span id="card-expected-${p.id}">${p.expectedUnits}</span></div>
            `;
            board.appendChild(card);
        }
    });
}

function updatePieceVisual(playerId, positionIndex, playerIndex) {
    const piece = document.getElementById(`player-${playerId}`);
    const cell = document.getElementById(`cell-${positionIndex}`);
    if (!piece || !cell) return;

    // 重なり防止：プレイヤーごとに位置をずらす
    const offsetX = (playerIndex % 2) * 18;
    const offsetY = Math.floor(playerIndex / 2) * 18;

    const cellRect = cell.getBoundingClientRect();
    const containerRect = document.querySelector('.board-container').getBoundingClientRect();

    piece.style.left = `${(cellRect.left - containerRect.left) + offsetX + 5}px`;
    piece.style.top = `${(cellRect.top - containerRect.top) + offsetY + 5}px`;
}

function handleTurnChange(nextId) {
    const diceBtn = document.getElementById('diceStart');
    const eventMsg = document.getElementById('event-message');
    const isMyTurn = (nextId && myPlayerId && nextId.trim() === myPlayerId.trim());

    if (isMyTurn) {
        diceBtn.disabled = false;
        diceBtn.style.opacity = "1";
        diceBtn.style.boxShadow = "0 0 15px #f1c40f";
        eventMsg.innerText = "あなたの番です！";
    } else {
        diceBtn.disabled = true;
        diceBtn.style.opacity = "0.5";
        diceBtn.style.boxShadow = "none";
        eventMsg.innerText = `${nextId} さんの番です...`;
    }
}

// アイテム選択
window.selectItem = (type) => {
    selectedItemType = type;
    document.getElementById('selected-item-display').innerText = "使用予定: ダブルダイス";
};
window.selectJust = (val) => {
    selectedItemType = 'JUST';
    selectedTargetValue = val;
    document.getElementById('selected-item-display').innerText = `使用予定: ジャストダイス (${val})`;
};
