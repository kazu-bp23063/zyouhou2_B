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
const error1 = document.querySelector('#error-msg1');
const error2 = document.querySelector('#error-msg2');

if(showLoginButton){
showLoginButton.addEventListener('click',(e) =>{
  e.preventDefault();
  loginForm.style.display = 'block';
  registerForm.style.display = 'none';
  alert('ログイン画面に移動しました!!');
   if(error1 || error2){
    error1.style.display = 'none';
    error2.style.display = 'none';
  }
})};

if(showRegisterButton){
showRegisterButton.addEventListener('click',function(e){
  e.preventDefault();
  loginForm.style.display = 'none';
  registerForm.style.display = 'block';
  alert('アカウント登録画面に移動しました!!');
   if(error1 || error2){
    error1.style.display = 'none';
    error2.style.display = 'none';
  }
   
})};

if(gameEnd){
gameEnd.addEventListener('click',function(){
  window.location.href = 'result.html';
})};

if(startButton){
startButton.addEventListener('click',function(){
  window.location.href = 'home.html';
})};

if(matchingStart.length>0){
  matchingStart.forEach(function(button){
    button.addEventListener('click',function(){
      window.location.href = "start.html";
    })
  })
}

if(gameStart){
  gameStart.addEventListener('click',function(){
    window.location.href = "game.html" 
  })
};




if(logout){
  logout.addEventListener('click',function(){
    const logoutConfirm = window.confirm("ログアウトしますか？");
    if(logoutConfirm){
      window.location.href= "/";
      
    }else{
      alert("ログアウトをキャンセルしました!!");
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
    let diceCount = parseInt(diceSelect.value); 
    let total = 0;

  for (let i = 0; i < diceCount; i++) {
      total += Math.floor(Math.random() * 6) + 1;
  }
    console.log('サイコロの結果', total);
    alert(`ユーザは${total}マス進みました!!!`);
  })
};


const statusModal = document.getElementById('statusModal');
const closeBtn = document.getElementById('closeModal');

if(statusButton){
  statusButton.addEventListener('click',function(){
    statusModal.style.display = "block";
  })
};

if(closeBtn){
    closeBtn.addEventListener('click', function(){
        statusModal.style.display = "none";
    });
}

window.addEventListener('click', function(event) {
    if (event.target == statusModal) {
        statusModal.style.display = "none";
    }
});

