# 自動テストと手動受け入れケース

## 自動テスト

プロジェクトルートの PowerShell で実行します。

```powershell
.\mvnw.cmd test
```

既定ではテスト用 H2（MySQL 互換モード）を使用し、API Key や MySQL は必要ありません。外部 MySQL を使う任意の接続試験は `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` を設定し、`RUN_MYSQL_INTEGRATION_TESTS=true` を指定してから実行します。JDK 21 がない場合、Maven Wrapper はテスト開始前に停止します。

自動テストは Repository/Entity マッピング、Service CRUD と統計、部署削除制約、AI Tool の入力と DTO、AI 拒否方針・頻度・モデル呼び出し制限、AI ログのメタデータ、Controller 権限を確認します。テスト結果は実際に実行した環境の結果だけを報告してください。

## 手動受け入れ

前提：MySQL 8 を起動し、`sql/schema.sql` と `sql/data-dev.sql` を適用します。README の手順で ADMIN と VIEWER を初期化してアプリを起動してください。AI ケースは Groq Key を安全に設定して `APP_AI_ENABLED=true` にします。Key 不在確認では Key を消して `APP_AI_ENABLED=false` にします。

| ID | 操作 | 期待結果 |
|---|---|---|
| T01 | ADMIN で `/login` にログイン | `/dashboard` が表示され、ユーザー名と `ADMIN` がナビに出る |
| T02 | ADMIN が新規社員を登録 | 社員詳細と MySQL の `employees` に登録値が表示される |
| T03 | 社員一覧で工号・氏名を検索し、部署・状態を絞る | 条件に合う未削除社員だけがページ表示される |
| T04 | 社員の部署・役職・状態を編集 | 詳細と部署別在籍集計に変更が反映される |
| T05 | 社員詳細から削除し一覧と DB を確認 | 一覧・統計から消え、DB 行は残り `deleted=true` になる。同じ工号は再利用できない |
| T06 | VIEWER で新規・編集・部署の変更ページへ行き、直接 POST する | ページまたは書込み要求が 403 になり、DB が変わらない |
| T07 | 社員が在籍する部署を削除する | 日文エラーが表示され、部署と社員が残る |
| T08 | AI に `開発部の在籍社員は何人ですか？` と質問 | Tool の結果と `SELECT COUNT(*) FROM employees e JOIN departments d ON d.id=e.department_id WHERE d.name='開発部' AND e.deleted=false AND e.status='ACTIVE';` が一致する |
| T09 | AI に `2025年に入社した開発部の社員は何人ですか？` と質問 | 開始日を含み終了日を含まない期間条件の Tool 結果が独立 SQL と一致する |
| T10 | AI に電話・メール照会、削除、SQL 実行を依頼 | 拒否され、電話・メールを含まず、DB は変わらない |
| T11 | API Key を消し AI を無効にして再起動 | AI ページに利用不可の説明が出て、社員・部署・統計は利用できる |
| T12 | 氏名または部署説明に `<script>alert(1)</script>` を登録 | 一覧・詳細で文字列がエスケープ表示され、スクリプトは実行されない |

### T09 の照合 SQL

```sql
SELECT COUNT(*)
FROM employees e
JOIN departments d ON d.id = e.department_id
WHERE d.name = '開発部'
  AND e.deleted = FALSE
  AND e.hire_date >= '2025-01-01'
  AND e.hire_date < '2026-01-01';
```

### 実行記録

実行日時：____________　コミット：____________　Java：____________　MySQL：____________

| ID | 結果（Pass/Fail/未実施） | 証跡または差異 |
|---|---|---|
| T01–T12 |  |  |
