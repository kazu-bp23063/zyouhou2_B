let currentCellIndex = 0;
const totalCells = 20;
let earnedUnits = 0;
let expectedUnits = 100;

/**
 * サイコロを振る
 */
async function useItemAndRoll() {
    const rollButton = document.getElementById('diceStart');
    const diceResultText = document.getElementById('dice-result-text');

    if (rollButton) rollButton.disabled = true;

    const roll = Math.floor(Math.random() * 6) + 1;
    if (diceResultText) diceResultText.innerText = `${roll} が出ました！`;

    await movePlayerStepByStep(roll);

    if (rollButton) rollButton.disabled = false;
}

/**
 * 駒の移動と単位・ゴール判定
 */
async function movePlayerStepByStep(steps) {
    for (let i = 0; i < steps; i++) {
        currentCellIndex = (currentCellIndex + 1) % totalCells;
        updatePlayerPosition();
        
        // START(0番)を通過・着地したとき
        if (currentCellIndex === 0) {
            earnedUnits += expectedUnits;
            
            // 画面の数字を更新
            const earnedDisplay = document.getElementById('earned-units');
            if (earnedDisplay) earnedDisplay.innerText = earnedUnits;

            // --- 追加: 124単位判定 ---
            if (earnedUnits >= 124) {
                document.getElementById('event-message').innerText = "124単位達成！卒業確定です！";
                // 少しだけ余韻を残してから移動
                await new Promise(r => setTimeout(r, 1000));
                window.location.href = "/result"; 
                return; // 124超えたら移動するのでループを抜ける
            }

            document.getElementById('event-message').innerText = "START通過！25単位獲得";
            await new Promise(r => setTimeout(r, 400));
        }
        await new Promise(r => setTimeout(r, 250));
    }
    document.getElementById('event-message').innerText = `${currentCellIndex}番のマスに停止中`;
}

/**
 * 駒の座標更新
 */
function updatePlayerPosition() {
    const player = document.getElementById('player');
    const target = document.getElementById(`cell-${currentCellIndex}`);
    if (!player || !target) return;
    
    player.style.left = `${target.offsetLeft + (target.offsetWidth / 2) - (player.offsetWidth / 2)}px`;
    player.style.top = `${target.offsetTop + (target.offsetHeight / 2) - (player.offsetHeight / 2)}px`;
}

// 初期化（モーダル処理など）
document.addEventListener('DOMContentLoaded', () => {
    const statusBtn = document.getElementById('status');
    const modal = document.getElementById('statusModal');
    
    if (statusBtn) {
        statusBtn.onclick = () => {
            document.getElementById('modal-pos').innerText = currentCellIndex;
            document.getElementById('modal-earned').innerText = earnedUnits;
            document.getElementById('modal-expected').innerText = expectedUnits;
            if (modal) modal.style.display = "block";
        };
    }
    
    const closeBtn = document.getElementById('closeModal');
    if (closeBtn) {
        closeBtn.onclick = () => { if (modal) modal.style.display = "none"; };
    }

    updatePlayerPosition();
});