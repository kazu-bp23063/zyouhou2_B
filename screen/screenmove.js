const showLoginButton = document.querySelector('#loginButton');
const showRegisterButton = document.querySelector('#registerButton');
const matchingStart = document.querySelectorAll('.matchingStart');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const dice = document.getElementById('dice');
const btn = document.getElementById('btn');
const gameEnd = document.querySelector('#gameEnd');
const startButton= document.querySelector('#start');
const gameStart = document.querySelector('#matching');
const rule = document.querySelector('#rule');
const score = document.querySelector('#score');
const logout = document.querySelector('#logout');
const diceSelect = document.querySelector('#diceSelect');
const diceWindow2 = document.querySelector('#diceWindow2');
const diceWindow3 = document.querySelector('#diceWindow3');
const diceStart = document.querySelector('#diceStart');
const statusButton = document.querySelector('#status');

if(btn){
btn.addEventListener('click', () => {
  
  if (dice.classList.contains('moving')) {
    dice.classList.remove('moving');
    btn.textContent = 'スタート'; 
  } else {
    dice.classList.add('moving');
    btn.textContent = 'ストップ'; 
  }
  
})};

if(showLoginButton){
showLoginButton.addEventListener('click',(e) =>{
  e.preventDefault();
  loginForm.style.display = 'block';
  registerForm.style.display = 'none';
})};

if(showRegisterButton){
showRegisterButton.addEventListener('click',function(e){
  e.preventDefault();
  loginForm.style.display = 'none';
  registerForm.style.display = 'block';

})};

if(gameEnd){
gameEnd.addEventListener('click',function(){
  window.location.href = '../result/result.html';
})};

if(startButton){
startButton.addEventListener('click',function(){
  window.location.href = '../home/home.html';
})};

if(matchingStart.length>0){
  matchingStart.forEach(function(button){
    button.addEventListener('click',function(){
      window.location.href = "../start/start.html";
    })
  })
}

if(gameStart){
  gameStart.addEventListener('click',function(){
    window.location.href = "../game/game.html" 
  })
};

if(rule){
  rule.addEventListener('click',function(){
    window.location.href = "../rule/rule.html" 
  })
};

if(score){
  score.addEventListener('click',function(){
    window.location.href = "../score/score.html" 
  })
};

if(logout){
  logout.addEventListener('click',function(){
    const logoutConfirm = window.confirm("ログアウトしますか？");

    if(logoutConfirm){
       window.location.href = "../home/home.html" 
    }else{
      
    }
  })
};


if(diceSelect){
  diceSelect.addEventListener('change',function(){
    console.log(diceSelect.value);
    if(diceSelect.value==1){
       diceWindow2.classList.add('none');
       diceWindow3.classList.add('none'); 
    }else if(diceSelect.value==2){
        diceWindow2.classList.remove('none');
        diceWindow3.classList.add('none');
    }else{
        diceWindow2.classList.remove('none');
        diceWindow3.classList.remove('none');
    }
})
};

if(diceStart){
  diceStart.addEventListener('click',function(){
    let diceResult = Math.floor(Math.random() * 6 * diceSelect.value) + 1;
    console.log('サイコロの結果',diceResult);
  }
)
};

/* ▼▼▼ 追加：モーダル要素と閉じるボタンを取得 ▼▼▼ */
const statusModal = document.getElementById('statusModal');
const closeBtn = document.getElementById('closeModal');

if(statusButton){
  statusButton.addEventListener('click',function(){
    statusModal.style.display = "block";
  })
};

/* ▼▼▼ 追加：×ボタンを押したらモーダルを隠す ▼▼▼ */
if(closeBtn){
    closeBtn.addEventListener('click', function(){
        statusModal.style.display = "none";
    });
}

/* おまけ：モーダルの外側（黒い部分）をクリックしても閉じるようにする */
window.addEventListener('click', function(event) {
    if (event.target == statusModal) {
        statusModal.style.display = "none";
    }
});