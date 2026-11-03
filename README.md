# 社員管理Webシステム

Java 21、Spring Boot 3.5.16、Spring Data JPA、MySQL 8、Thymeleaf、Bootstrap 5、Spring Security 6 を使った単一モジュールの Web アプリです。AI は Spring AI 1.1.8 の OpenAI 互換 Chat API と読み取り専用 Tool Calling を使い、既定では Groq の無料枠（Qwen 3.8 27B）へ接続します。無料枠には利用量とリクエスト頻度の上限があり、提供内容はサービス側で変更されることがあります。Groq の無料枠に留め、Billing で有料プランに切り替えないでください。Spring AI 1.1.x は Spring Boot 3.5.x 系を対象とし、1.1.8 のリリース依存は Boot 3.5.15 です。本プロジェクトは同じ Boot 3.5 系の 3.5.16 を使います。[Spring AI リリース](https://github.com/spring-projects/spring-ai) [互換性](https://github.com/spring-projects/spring-ai) [Groq 無料枠の上限](https://console.groq.com/docs/rate-limits) [Groq Tool Calling](https://console.groq.com/docs/tool-use/overview)

AI API Key がなくても、社員管理・部署管理・ログイン・統計機能は起動します。AI は既定で無効です。AI を有効にしたときだけモデルクライアントを作るため、空の Key で起動時に失敗しません。既定の無料プロバイダーは Groq です。キーは Groq Console で作成し、`APP_AI_API_KEY` というプロセス環境変数で渡します。OpenAI 互換 API を提供する他のモデルサービスを使う場合は、接続先 URL とモデル名も環境変数で変更できます。

## 必要な環境

- JDK 21
- MySQL 8.0.16 以上
- Windows PowerShell 5.1 以降
- 初回ビルド時に Apache Maven 3.9.16 と Maven Central へ接続できるネットワーク

<code>mvnw.cmd</code> は初回実行時に Maven 3.9.16 をダウンロードして SHA-512 を照合します。Maven の個別インストールは不要です。JDK と MySQL は別途必要です。

## Windows PowerShell での準備と起動

プロジェクトのルートで実行します。

1. MySQL を起動し、スキーマと開発データを一度だけ読み込みます。

   ~~~powershell
   mysql --default-character-set=utf8mb4 -u root -p -e "source sql/schema.sql"
   mysql --default-character-set=utf8mb4 -u root -p -e "source sql/data-dev.sql"
   ~~~

   初期データは 6 部署と EMP0001〜EMP0030 の架空社員です。各メールは予約ドメイン <code>.invalid</code> を使います。再実行しても同じ部署名・工号は重複登録されません。

2. DB 接続情報を現在の PowerShell セッションに設定します。既存の MySQL ユーザーに <code>employee_management</code> へのアクセス権が必要です。

   ~~~powershell
   $env:DB_URL = 'jdbc:mysql://localhost:3306/employee_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Tokyo'
   $env:DB_USERNAME = 'root'

   function Set-SecretEnvironmentVariable([string]$Name, [string]$Prompt) {
     $secure = Read-Host $Prompt -AsSecureString
     $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
     try {
       [Environment]::SetEnvironmentVariable($Name, [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer), 'Process')
     } finally {
       [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
     }
   }

   Set-SecretEnvironmentVariable 'DB_PASSWORD' 'MySQL password'
   ~~~

3. 初回起動時に開発用 ADMIN/VIEWER を BCrypt で作成する場合、セッション内でパスワードを設定します。パスワードは12文字以上にしてください。初期化は <code>APP_DEMO_USERS_ENABLED=true</code> のときだけ行われます。

   ~~~powershell
   $env:APP_DEMO_USERS_ENABLED = 'true'
   $env:APP_DEMO_ADMIN_USERNAME = 'admin'
   $env:APP_DEMO_VIEWER_USERNAME = 'viewer'
   Set-SecretEnvironmentVariable 'APP_DEMO_ADMIN_PASSWORD' 'ADMIN password (12+ characters)'
   Set-SecretEnvironmentVariable 'APP_DEMO_VIEWER_PASSWORD' 'VIEWER password (12+ characters)'
   ~~~

   アカウントが既に存在する場合、起動時の初期化はそのパスワードを変更しません。実運用の資格情報をソースや設定ファイルに保存しないでください。

4. AI を使わない場合は無効のままにし、テストして起動します。

   ~~~powershell
   $env:APP_AI_ENABLED = 'false'
   .\mvnw.cmd test
   .\mvnw.cmd spring-boot:run
   ~~~

   ブラウザーで <code>http://localhost:8080/login</code> を開きます。Hibernate は <code>ddl-auto=validate</code> で起動時に表構造を検証します。建表は <code>sql/schema.sql</code> で行ってください。アプリケーションを起動した PowerShell はそのまま残します。

5. AI を有効にする場合、アプリを停止して次の値を設定してから再起動します。

   ~~~powershell
   $env:APP_AI_ENABLED = 'true'
   $env:APP_AI_BASE_URL = 'https://api.groq.com/openai'
   $env:APP_AI_MODEL = 'qwen/qwen3.8-27b'
   Set-SecretEnvironmentVariable 'APP_AI_API_KEY' 'Groq API key'
   .\mvnw.cmd spring-boot:run
   ~~~

   AI には質問と、許可された検索 Tool の結果だけが外部モデルサービスへ送信されます。デモデータ以外の実在個人情報は使わないでください。

6. セッション環境変数を消す場合：

   ~~~powershell
   Remove-Item Env:DB_PASSWORD, Env:DB_USERNAME, Env:DB_URL, Env:APP_DEMO_USERS_ENABLED, Env:APP_DEMO_ADMIN_PASSWORD, Env:APP_DEMO_VIEWER_PASSWORD, Env:APP_AI_API_KEY, Env:APP_AI_BASE_URL, Env:APP_AI_MODEL, Env:APP_AI_ENABLED -ErrorAction SilentlyContinue
   ~~~

## 機能

- ADMIN/VIEWER の Form Login、BCrypt、Session、CSRF 保護
- 社員一覧のページング、氏名・工号検索、部署・状態フィルター
- 社員の作成、編集、詳細、論理削除
- 部署の作成、名称・説明変更、社員が参照する部署の削除拒否
- 社員数、在籍数、部署数、当年入社数と部署別在籍数
- 日文 AI 自然言語照会と、最大20件・個人連絡先を除外した結果
- AI 無効・Key 不在・外部サービス障害時の明確な縮退応答
- AI 照会で実行した固定 Tool 名・結果状態・所要時間・日時のメタデータ記録（質問・回答本文は保存しない）

## テスト

既定の <code>.\mvnw.cmd test</code> は H2 の MySQL 互換モードを使うため、MySQL サーバーや API Key がなくても実行できます。カバーする内容：

ソースは JDK 21 でコンパイル確認済みです。テストコードは用意していますが、今回の公開前に <code>.\mvnw.cmd test</code> は実行していないため、テスト結果は未確認です。MySQL を使ったブラウザー手動試験も未実施です。実際の画面キャプチャや実験測定値は作成していません。

- JPA Entity と Repository の関連・Enum・論理削除マッピング
- 社員作成、重複工号、更新、論理削除、AI 検索上限20件
- 部署名重複、参照中部署削除拒否、空部署削除
- Dashboard の集計条件
- AI Tool の日付・状態入力、白名单 DTO、拒否方針、頻度とモデル呼び出し上限
- 未ログイン、VIEWER、ADMIN のアクセス制御、CSRF、AI 無効時の応答
- API Key がない状態で Spring コンテキストが起動すること
- AI 照会ログに質問・回答・個人情報が含まれず、メタデータだけ記録されること

MySQL 接続と実テーブル検証を追加で行う場合、データベース・環境変数を設定し、以下を実行します。

~~~powershell
$env:RUN_MYSQL_INTEGRATION_TESTS = 'true'
.\mvnw.cmd test
~~~

実行結果は実際にコマンドを実行した環境のものだけを報告してください。手動シナリオは [docs/test-cases.md](docs/test-cases.md) にあります。AI 実験用の未記入テンプレートは [docs/ai-experiment-template.csv](docs/ai-experiment-template.csv) です。実行していないスクリーンショットや測定値は含めていません。

## 構成とデータ保護

アーキテクチャ、テーブル関係、卒業報告の章立ては [docs/architecture.md](docs/architecture.md)、手動受け入れ手順は [docs/test-cases.md](docs/test-cases.md) を参照してください。

- <code>departments</code> 1 — N <code>employees</code>; 社員から部署への外部キーは <code>ON DELETE RESTRICT</code>。
- <code>app_users</code> はログイン認証用。AI Query Tool はユーザーテーブルに触れません。
- <code>ai_query_logs</code> は固定 Tool 名・結果状態・所要時間・時刻だけを保存し、問答全文を保存しません。
- 工号は論理削除後も一意。社員統計は <code>deleted=false</code> の行だけを数えます。
- AI Tool は5種類の固定読み取り操作のみ公開し、任意SQLや更新操作を持ちません。
- AI へ渡す社員 DTO は工号、氏名、部署、役職、入社日、状態だけです。電話・メール・パスワード Hash を含めません。
- 外部呼び出しは接続 timeout 5秒・応答 timeout 20秒、再試行なし、1問い合わせ6モデル呼び出しまで、ユーザーごとに1分10回までに制限しています。
- AI には全状態の社員総数を数える Tool がないため、その質問には対応できる範囲を案内し、検索上限20件から総数を推測しません。

## バージョンと参考資料

- [Spring Boot 3.5 reference](https://docs.spring.io/spring-boot/3.5/documentation.html)
- [Spring Boot 3.5 system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Spring AI 1.1.8 Tool Calling](https://docs.spring.io/spring-ai/reference/1.1/api/tools.html)
- [Spring AI 1.1.8 Chat Client](https://docs.spring.io/spring-ai/reference/1.1/api/chatclient.html)
- [Spring AI 1.1.x / Spring Boot 3.5 compatibility](https://github.com/spring-projects/spring-ai)
