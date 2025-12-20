document.addEventListener('DOMContentLoaded', () => {
    
    // 現在の人数（自分1人）
    let currentPlayers = 1;
    const maxPlayers = 4;
    
    // HTMLの要素を取得
    const countDisplay = document.getElementById('current-count');

    // プレイヤーを追加する関数
    function addDummyPlayer(slotId, playerName) {
        const slot = document.getElementById(slotId);
        
        // まだ接続されていない(connectedクラスがない)場合のみ実行
        if(slot && !slot.classList.contains('connected')) {
            slot.classList.add('connected'); // 色を変えるクラスを追加
            
            // アイコンと名前を書き換える
            const iconPart = slot.querySelector('.icon');
            const namePart = slot.querySelector('.name');
            
            if(iconPart) iconPart.textContent = '●';
            if(namePart) namePart.textContent = playerName;

            // 人数を増やして表示更新
            currentPlayers++;
            if(countDisplay) countDisplay.textContent = currentPlayers;

            console.log(`${playerName} が参加しました。現在 ${currentPlayers} 人`);

            // 4人揃ったらゲーム画面へ移動
            if(currentPlayers >= maxPlayers) {
                setTimeout(() => {
                    alert('マッチング成立！ゲームを開始します。');
                    window.location.href = '/game'; 
                }, 500);
            }
        }
    }

    // ▼▼ 自動で人が入ってくるシミュレーション ▼▼
    
    // 1.5秒後にゲストAが入室（p2枠）
    setTimeout(() => {
        addDummyPlayer('p2', 'ゲストA');
    }, 1500);

    // 3秒後にゲストBが入室（p3枠）
    setTimeout(() => {
        addDummyPlayer('p3', 'ゲストB');
    }, 3000);

    // 4.5秒後にゲストCが入室（p4枠）
    setTimeout(() => {
        addDummyPlayer('p4', 'ゲストC');
    }, 4500);

});