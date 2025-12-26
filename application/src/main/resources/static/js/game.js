    /**
 * game.js (WebSocket ターン同期強化版)
 */

// 1. URLパラメータから情報を取得
const urlParams = new URLSearchParams(window.location.search);
const myPlayerId = urlParams.get('playerId');
const roomId = urlParams.get('roomId');

// 2. WebSocket接続 (ポート8080の共通窓口)
const socket = new WebSocket("ws://localhost:8080/client-management");

// アイテムの状態管理
let selectedItemType = null;
let selectedTargetValue = null;

document.addEventListener('DOMContentLoaded', async () => {
    const diceBtn = document.getElementById('diceStart');
    const eventMsg = document.getElementById('event-message');
    const earnedUnitsDisplay = document.getElementById('earned-units');

    // --- 3. WebSocket 接続完了時の処理 ---
    socket.onopen = () => {
        console.log("Game WebSocket Connected. PlayerID:", myPlayerId);
        // 【重要】サーバーに自分を登録し、プッシュ通知を受け取れるようにする
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
            if (diceResult) diceResult.innerText = data.diceValue;

            // ② 駒の移動を実行
            updatePieceVisual(data.lastPlayerId, data.newPosition);

            // ③ 単位の更新（自分の番が終わった時、または誰かが動いた時）
            if (data.lastPlayerId === myPlayerId) {
                earnedUnitsDisplay.innerText = data.earnedUnits;
                document.getElementById('modal-earned').innerText = data.earnedUnits;
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

/**
 * 手番に応じてUI（ボタン・メッセージ）を更新する
 */
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

/**
 * 駒を生成して配置する
 */
function setupPlayersUI(players) {
    const container = document.querySelector('.board-container');
    players.forEach((p) => {
        if (!document.getElementById(`player-${p.id}`)) {
            const piece = document.createElement('div');
            piece.id = `player-${p.id}`;
            piece.className = 'player-piece'; //
            piece.style.backgroundColor = p.color;
            piece.innerText = p.name.substring(0, 1);
            container.appendChild(piece);
        }
        updatePieceVisual(p.id, p.currentPosition);
    });
}

/**
 * 駒を指定のセル位置へ移動させる
 */
function updatePieceVisual(playerId, cellIndex) {
    const piece = document.getElementById(`player-${playerId}`);
    const cell = document.getElementById(`cell-${cellIndex}`); //
    if (piece && cell) {
        // 複数人重なった時のためのオフセット
        const offset = (playerId.length % 5) * 5;
        piece.style.top = `${cell.offsetTop + 5 + offset}px`;
        piece.style.left = `${cell.offsetLeft + 5 + offset}px`;
        
        if (playerId === myPlayerId) {
            const modalPos = document.getElementById('modal-pos');
            if (modalPos) modalPos.innerText = cellIndex;
        }
    }
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