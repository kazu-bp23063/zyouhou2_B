// 1. URLパラメータから情報を取得
const urlParams = new URLSearchParams(window.location.search);
const myPlayerId = urlParams.get('playerId');
const roomId = urlParams.get('roomId');
const statusBtn = document.getElementById('status');
const modal = document.getElementById('statusModal');
const closeBtn = document.getElementById('closeModal');

// 2. WebSocket接続 (ポート8080の共通窓口)
const socket = new WebSocket("ws://localhost:8080/game-server");

// アイテムの状態管理
let selectedItemType = null;
let selectedTargetValue = null;

// （三平）プレイヤーID -> 表示インデックス を保持（重なり防止のため）
const playerIndexMap = new Map(); // （三平）

document.addEventListener('DOMContentLoaded', async () => {
    const diceBtn = document.getElementById('diceStart');
    const eventMsg = document.getElementById('event-message');
    const earnedUnitsDisplay = document.getElementById('earned-units');

    // --- 3. WebSocket 接続完了時の処理 ---
    socket.onopen = () => {
        console.log("Game WebSocket Connected. PlayerID:", myPlayerId);
        const joinMsg = {
            taskName: "GAME_JOIN",
            roomId: roomId,
            playerId: myPlayerId
        };
        socket.send(JSON.stringify(joinMsg));
    };

    // --- 4. サーバーからの更新通知を処理 ---
    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("--- 受信データ ---", data);

        if (data.taskName === "GAME_UPDATE") {

            // ① 出目の表示
            const diceResult = document.querySelector('#dice-result-text span');
            document.getElementById('event-message').innerText = data.message;
            if (diceResult) diceResult.innerText = data.diceValue;

            // （三平）playerIndex を lastPlayerId から復元（固定0をやめて重なり防止）
            const pIndex = playerIndexMap.has(data.lastPlayerId) ? playerIndexMap.get(data.lastPlayerId) : 0; // （三平）

            // ② 駒の移動を実行
            updatePieceVisual(data.lastPlayerId, data.newPosition, pIndex);

            // （三平）マス移動が終わってからポップアップを出す（描画後に出したいので setTimeout）
            if (data.eventMessage) { // （三平）
                setTimeout(() => alert(data.eventMessage), 0); // （三平）
            }

            // ③ 単位の更新（自分の番が終わった時、または誰かが動いた時）
            if (data.lastPlayerId === myPlayerId) {
                earnedUnitsDisplay.innerText = data.earnedUnits;
                document.getElementById('modal-earned').innerText = data.earnedUnits;

                // （三平）予定単位もサーバーから来ていれば反映（必要ないなら消してOK）
                if (data.expectedUnits !== undefined && data.expectedUnits !== null) {
                    const expectedUnitsDisplay = document.getElementById('expected-units');
                    const modalExpected = document.getElementById('modal-expected');
                    if (expectedUnitsDisplay) expectedUnitsDisplay.innerText = data.expectedUnits; // （三平）
                    if (modalExpected) modalExpected.innerText = data.expectedUnits; // （三平）
                }
            }

            // ④ 【重要】ターンの判定とボタン制御
            handleTurnChange(data.nextPlayerId);

            // ⑤ 卒業判定
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
        // 二重送信防止
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
    window.onclick = (e) => { if (e.target == modal) modal.style.display = "none"; };
});


function handleTurnChange(nextId) {
    const diceBtn = document.getElementById('diceStart');
    const eventMsg = document.getElementById('event-message');

    // IDを比較（念のため空白を消去して比較）
    const isMyTurn = (nextId && myPlayerId && nextId.trim() === myPlayerId.trim());
    console.log(`次の方: ${nextId} | 判定結果: ${isMyTurn ? "自分の番" : "待機"}`);

    if (isMyTurn) {
        diceBtn.disabled = false;
        diceBtn.style.opacity = "1";
        diceBtn.style.cursor = "pointer";
        diceBtn.style.boxShadow = "0 0 15px #f1c40f";
        eventMsg.innerText = "あなたの番です！";
    } else {
        diceBtn.disabled = true;
        diceBtn.style.opacity = "0.5";
        diceBtn.style.cursor = "not-allowed";
        diceBtn.style.boxShadow = "none";
        eventMsg.innerText = `${nextId} さんの番です...`;
    }
}

const myColor = urlParams.get('color'); // URLから自分の色を復元

function setupPlayersUI(players) {
    const container = document.querySelector('.board-container');
    players.forEach((p, i) => {

        // （三平）IDごとのインデックスを固定保存（重なり防止のため）
        if (!playerIndexMap.has(p.id)) playerIndexMap.set(p.id, i); // （三平）

        let piece = document.getElementById(`player-${p.id}`);
        if (!piece) {
            piece = document.createElement('div');
            piece.id = `player-${p.id}`;
            piece.className = 'player-piece';
            container.appendChild(piece);
        }
        // 色を適用
        piece.style.backgroundColor = p.color;
        piece.style.background = p.color; 
        piece.innerText = p.name.substring(0, 1);

        updatePieceVisual(p.id, p.currentPosition, i); // インデックスを渡す
    });
}

function updatePieceVisual(playerId, positionIndex, playerIndex) {
    const piece = document.getElementById(`player-${playerId}`);
    const cell = document.getElementById(`cell-${positionIndex}`);
    if (!piece || !cell) return;

    // 重なり防止：プレイヤー番号に応じて位置をずらす
    const offsetX = (playerIndex % 2) * 15;
    const offsetY = Math.floor(playerIndex / 2) * 15;

    const cellRect = cell.getBoundingClientRect();
    const containerRect = document.querySelector('.board-container').getBoundingClientRect();

    piece.style.left = `${(cellRect.left - containerRect.left) + offsetX + 5}px`;
    piece.style.top = `${(cellRect.top - containerRect.top) + offsetY + 5}px`;
}

// アイテム選択用（HTMLのonclickから呼ばれる）
window.selectItem = (type) => {
    selectedItemType = type;
    document.getElementById('selected-item-display').innerText = "使用予定: ダブルダイス";
};
window.selectJust = (val) => {
    selectedItemType = 'JUST';
    selectedTargetValue = val;
    document.getElementById('selected-item-display').innerText = `使用予定: ジャストダイス (${val})`;
};
