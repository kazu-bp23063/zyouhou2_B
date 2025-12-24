let pollingInterval;

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    let roomId = urlParams.get('roomId');

    if (!roomId) {
        initializeMatching();
    } else {
        // 直接URLで来た場合（テスト用など）はIDがないので再取得が必要
        console.warn("直接アクセスはIDが欠損する可能性があります");
    }
});

async function initializeMatching() {
    try {
        const playerName = "Player_" + Math.floor(Math.random() * 100);
        const res = await fetch(`/api/matching/auto-join?playerName=${encodeURIComponent(playerName)}`, { method: 'POST' });

        // 一度テキストとして受け取って中身を確認する（デバッグに便利）
        const text = await res.text();
        console.log("サーバーからの生データ:", text);

        if (!text || text.trim() === "") {
            throw new Error("サーバーからのレスポンスが空です。Java側のControllerを確認してください。");
        }

        // ここで data を宣言（1回だけにします）
        const data = JSON.parse(text);
        console.log("パース後のデータ:", data);

        const roomId = data.room.roomId;
        const myId = data.me.id;
        const myColor = data.me.color;

        console.log(`入室成功！ Room: ${roomId}, MyID: ${myId}, Color: ${myColor}`);
        history.replaceState(null, '', `?roomId=${roomId}&playerId=${myId}&color=${encodeURIComponent(myColor)}`);
        console.log("サーバーから届いた自分のデータ:", data.me); // ここで color が入っているか確認
        console.log("割り当てられた色:", data.me.color);

        startPolling(roomId, myId, myColor);
    } catch (err) {
        console.error("マッチングエラー:", err);
    }
}

function startPolling(roomId, myId, myColor) {
    pollingInterval = setInterval(async () => {
        const res = await fetch(`/api/matching/status?roomId=${roomId}`);
        const room = await res.json();

        updateUI(room.players);

        if (room.players.length >= 4) {
            clearInterval(pollingInterval);
            console.log("4人揃いました。遷移します。");
            setTimeout(() => {
                // ここでIDを渡すのが最重要
window.location.href = `/game?roomId=${roomId}&playerId=${myId}&color=${encodeURIComponent(myColor)}`;
            }, 2000);
        }
    }, 2000);
}

function updateUI(players) {
    for (let i = 1; i <= 4; i++) {
        const slot = document.getElementById(`p${i}`);
        if (!slot) continue;
        
        const p = players[i-1]; // サーバーから来たi番目のプレイヤー
        const nameLabel = slot.querySelector('.name');
        const iconLabel = slot.querySelector('.icon');

        if (p) {
            // プレイヤーが存在する場合
            slot.classList.add('connected');
            nameLabel.innerText = p.name; // サーバー側の名前を表示
            iconLabel.innerText = "👤";
        } else {
            // 空席の場合
            slot.classList.remove('connected');
            nameLabel.innerText = "待機中...";
            iconLabel.innerText = "?";
        }
    }
    document.getElementById('current-count').innerText = players.length;
}
