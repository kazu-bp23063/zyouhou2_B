# 情報実験II_B班

## ServerAddress
ClientManagementServer  
WebSocket: ws://IPアドレス:8080  
REST API: http://IPアドレス:8082  

ApplicationServer  
WebSocket: ws://IPアドレス:8081  
REST API: http://IPアドレス:8081

## IPアドレスの設定箇所
接続先を変える場合は、以下を自分の環境のIPに書き換えてください。

- `application/src/main/resources/application.properties`
  - `client.management.rest.base` (例: `http://<管理サーバIP>:8082`)
  - `client.management.ws.uri`   (例: `ws://<管理サーバIP>:8080/app/matching`)
  - `app.server.rest.base`       (例: `http://<アプリサーバIP>:8081/api`)
  - `app.server.ws.uri`          (例: `ws://<アプリサーバIP>:8081/game-server`)
- `application/src/main/java/com/example/application/ClientManagementServer/Controller/MatchingManagement.java`
  - `sendRoomToAppServer` 内の `appBase`（`http://<アプリサーバIP>:8081/api`）

## IPアドレスの調べ方
コマンドプロンプトで `ipconfig` を実行し、Wireless LAN adapter Wi-Fi の IPv4 アドレスを確認してください。

## 実行方法
1. ClientManagementServer を起動（`ManagementServerLauncher` を実行）  
2. ApplicationServer を起動（`ApplicationServerLauncher` を実行）  
3. クライアント（Client）を起動（`Application` を実行）  
4. ブラウザで `http://localhost:8000` にアクセスし、ログイン → マッチング待機 → ゲーム開始

## 編集方法

各メンバーのブランチでコミットプッシュする

mainブランチにマージする際にはプルリクエストをする

javaファイルはそれぞれの[src/main/java/com/example/application]に入れる

HTMLファイルは[src/main/resources/templates]に入れる

CSSファイルは[src/main/resources/static/css]に入れる

JSファイルは[src/main/resources/static/js]に入れる

画像ファイルは[src/main/resources/static/images]に入れる











