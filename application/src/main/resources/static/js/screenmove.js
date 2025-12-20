const showLoginButton = document.querySelector('#loginButton');
const showRegisterButton = document.querySelector('#registerButton');
const logout = document.querySelector('#logout');
const statusModal = document.getElementById('statusModal');
const closeBtn = document.getElementById('closeModal');
const statusButton = document.getElementById('statusButton');


if(showLoginButton){
showLoginButton.addEventListener('click',(e) =>{
  e.preventDefault();
  loginForm.style.display = 'block';
  registerForm.style.display = 'none';
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
   if(error1 || error2){
    error1.style.display = 'none';
    error2.style.display = 'none';
  }
   
})};


if(logout){
  logout.addEventListener('click', function(e){
    // formのデフォルトの動きを止める（もしform内にある場合）
    e.preventDefault();
    const logoutConfirm = window.confirm("ログアウトしますか？");
    if(logoutConfirm){
      // 直接 / に行くのではなく、コントローラーのログアウト処理を呼ぶ
      window.location.href = "/logout"; 
    } else {
      alert("ログアウトをキャンセルしました!!");
    }
  });
}

if(gameStart){
  gameStart.addEventListener('click',function(){
    window.location.href = "/matchingwait" 
  })
};




if(logout){
  logout.addEventListener('click', function(e){
    e.preventDefault();
    const logoutConfirm = window.confirm("ログアウトしますか？");
    if(logoutConfirm){
      window.location.href = "/logout"; 
    }
  });
}

// 【エラー解消箇所】定義したあとなら if 文が正しく動作します
if(statusButton && statusModal){
  statusButton.addEventListener('click', function(){
    statusModal.style.display = "block";
  });
}

if(closeBtn && statusModal){
    closeBtn.addEventListener('click', function(){
        statusModal.style.display = "none";
    });
}

window.addEventListener('click', function(event) {
    if (statusModal && event.target == statusModal) {
        statusModal.style.display = "none";
    }
});