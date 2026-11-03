SET NAMES utf8mb4;
USE employee_management;

-- Restore the six bundled demo departments by their original seed IDs.
UPDATE departments
SET name = CASE id
        WHEN 1 THEN '開発部'
        WHEN 2 THEN '人事部'
        WHEN 3 THEN '営業部'
        WHEN 4 THEN '総務部'
        WHEN 5 THEN '経理部'
        WHEN 6 THEN '情報システム部'
    END,
    description = CASE id
        WHEN 1 THEN '社内システムの開発を担当'
        WHEN 2 THEN '採用と人材育成を担当'
        WHEN 3 THEN '法人営業を担当'
        WHEN 4 THEN '社内運営を担当'
        WHEN 5 THEN '会計業務を担当'
        WHEN 6 THEN '社内 IT 環境を担当'
    END
WHERE id BETWEEN 1 AND 6;

-- Restore names and job titles for the bundled fictional employees only.
UPDATE employees
SET full_name = CONCAT(
        'テスト社員',
        LPAD(CAST(SUBSTRING(employee_code, 4) AS UNSIGNED), 2, '0')
    ),
    job_title = CASE employee_code
        WHEN 'EMP0001' THEN 'ソフトウェアエンジニア'
        WHEN 'EMP0002' THEN '人事担当'
        WHEN 'EMP0003' THEN '営業担当'
        WHEN 'EMP0004' THEN '総務担当'
        WHEN 'EMP0005' THEN '経理担当'
        WHEN 'EMP0006' THEN 'システム管理者'
        WHEN 'EMP0007' THEN 'QA エンジニア'
        WHEN 'EMP0008' THEN '採用担当'
        WHEN 'EMP0009' THEN '営業担当'
        WHEN 'EMP0010' THEN '施設管理担当'
        WHEN 'EMP0011' THEN '会計担当'
        WHEN 'EMP0012' THEN 'ヘルプデスク担当'
        WHEN 'EMP0013' THEN 'バックエンドエンジニア'
        WHEN 'EMP0014' THEN '人材開発担当'
        WHEN 'EMP0015' THEN '営業担当'
        WHEN 'EMP0016' THEN '総務担当'
        WHEN 'EMP0017' THEN '経理担当'
        WHEN 'EMP0018' THEN 'ネットワーク担当'
        WHEN 'EMP0019' THEN 'フロントエンドエンジニア'
        WHEN 'EMP0020' THEN '人事担当'
        WHEN 'EMP0021' THEN '営業担当'
        WHEN 'EMP0022' THEN '文書管理担当'
        WHEN 'EMP0023' THEN '財務担当'
        WHEN 'EMP0024' THEN 'システム運用担当'
        WHEN 'EMP0025' THEN 'データベース担当'
        WHEN 'EMP0026' THEN '採用担当'
        WHEN 'EMP0027' THEN '営業担当'
        WHEN 'EMP0028' THEN '総務担当'
        WHEN 'EMP0029' THEN '会計担当'
        WHEN 'EMP0030' THEN 'IT サポート担当'
    END
WHERE employee_code BETWEEN 'EMP0001' AND 'EMP0030';
