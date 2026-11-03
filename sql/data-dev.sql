SET NAMES utf8mb4;
USE employee_management;

-- 固定の架空データです。INSERT IGNORE により再実行時の重複を避けます。
INSERT IGNORE INTO departments (name, description) VALUES
    ('開発部', '社内システムの開発を担当'),
    ('人事部', '採用と人材育成を担当'),
    ('営業部', '法人営業を担当'),
    ('総務部', '社内運営を担当'),
    ('経理部', '会計業務を担当'),
    ('情報システム部', '社内 IT 環境を担当');

INSERT IGNORE INTO employees
    (employee_code, full_name, email, phone, department_id, job_title, hire_date, status)
VALUES
    ('EMP0001', 'テスト社員01', 'employee01@example.invalid', '000-0000-0001', (SELECT id FROM departments WHERE name = '開発部'), 'ソフトウェアエンジニア', '2024-04-01', 'ACTIVE'),
    ('EMP0002', 'テスト社員02', 'employee02@example.invalid', '000-0000-0002', (SELECT id FROM departments WHERE name = '人事部'), '人事担当', '2024-04-15', 'ACTIVE'),
    ('EMP0003', 'テスト社員03', 'employee03@example.invalid', '000-0000-0003', (SELECT id FROM departments WHERE name = '営業部'), '営業担当', '2024-05-01', 'ACTIVE'),
    ('EMP0004', 'テスト社員04', 'employee04@example.invalid', '000-0000-0004', (SELECT id FROM departments WHERE name = '総務部'), '総務担当', '2024-05-15', 'LEAVE'),
    ('EMP0005', 'テスト社員05', 'employee05@example.invalid', '000-0000-0005', (SELECT id FROM departments WHERE name = '経理部'), '経理担当', '2024-06-01', 'ACTIVE'),
    ('EMP0006', 'テスト社員06', 'employee06@example.invalid', '000-0000-0006', (SELECT id FROM departments WHERE name = '情報システム部'), 'システム管理者', '2024-06-15', 'RESIGNED'),
    ('EMP0007', 'テスト社員07', 'employee07@example.invalid', '000-0000-0007', (SELECT id FROM departments WHERE name = '開発部'), 'QA エンジニア', '2024-07-01', 'ACTIVE'),
    ('EMP0008', 'テスト社員08', 'employee08@example.invalid', '000-0000-0008', (SELECT id FROM departments WHERE name = '人事部'), '採用担当', '2024-07-15', 'ACTIVE'),
    ('EMP0009', 'テスト社員09', 'employee09@example.invalid', '000-0000-0009', (SELECT id FROM departments WHERE name = '営業部'), '営業担当', '2024-08-01', 'LEAVE'),
    ('EMP0010', 'テスト社員10', 'employee10@example.invalid', '000-0000-0010', (SELECT id FROM departments WHERE name = '総務部'), '施設管理担当', '2024-08-15', 'ACTIVE'),
    ('EMP0011', 'テスト社員11', 'employee11@example.invalid', '000-0000-0011', (SELECT id FROM departments WHERE name = '経理部'), '会計担当', '2024-09-01', 'ACTIVE'),
    ('EMP0012', 'テスト社員12', 'employee12@example.invalid', '000-0000-0012', (SELECT id FROM departments WHERE name = '情報システム部'), 'ヘルプデスク担当', '2024-09-15', 'ACTIVE'),
    ('EMP0013', 'テスト社員13', 'employee13@example.invalid', '000-0000-0013', (SELECT id FROM departments WHERE name = '開発部'), 'バックエンドエンジニア', '2025-01-06', 'ACTIVE'),
    ('EMP0014', 'テスト社員14', 'employee14@example.invalid', '000-0000-0014', (SELECT id FROM departments WHERE name = '人事部'), '人材開発担当', '2025-01-20', 'ACTIVE'),
    ('EMP0015', 'テスト社員15', 'employee15@example.invalid', '000-0000-0015', (SELECT id FROM departments WHERE name = '営業部'), '営業担当', '2025-02-03', 'RESIGNED'),
    ('EMP0016', 'テスト社員16', 'employee16@example.invalid', '000-0000-0016', (SELECT id FROM departments WHERE name = '総務部'), '総務担当', '2025-02-17', 'ACTIVE'),
    ('EMP0017', 'テスト社員17', 'employee17@example.invalid', '000-0000-0017', (SELECT id FROM departments WHERE name = '経理部'), '経理担当', '2025-03-03', 'ACTIVE'),
    ('EMP0018', 'テスト社員18', 'employee18@example.invalid', '000-0000-0018', (SELECT id FROM departments WHERE name = '情報システム部'), 'ネットワーク担当', '2025-03-17', 'LEAVE'),
    ('EMP0019', 'テスト社員19', 'employee19@example.invalid', '000-0000-0019', (SELECT id FROM departments WHERE name = '開発部'), 'フロントエンドエンジニア', '2025-04-01', 'ACTIVE'),
    ('EMP0020', 'テスト社員20', 'employee20@example.invalid', '000-0000-0020', (SELECT id FROM departments WHERE name = '人事部'), '人事担当', '2025-04-14', 'ACTIVE'),
    ('EMP0021', 'テスト社員21', 'employee21@example.invalid', '000-0000-0021', (SELECT id FROM departments WHERE name = '営業部'), '営業担当', '2025-05-01', 'ACTIVE'),
    ('EMP0022', 'テスト社員22', 'employee22@example.invalid', '000-0000-0022', (SELECT id FROM departments WHERE name = '総務部'), '文書管理担当', '2025-06-02', 'RESIGNED'),
    ('EMP0023', 'テスト社員23', 'employee23@example.invalid', '000-0000-0023', (SELECT id FROM departments WHERE name = '経理部'), '財務担当', '2025-07-01', 'ACTIVE'),
    ('EMP0024', 'テスト社員24', 'employee24@example.invalid', '000-0000-0024', (SELECT id FROM departments WHERE name = '情報システム部'), 'システム運用担当', '2025-08-01', 'ACTIVE'),
    ('EMP0025', 'テスト社員25', 'employee25@example.invalid', '000-0000-0025', (SELECT id FROM departments WHERE name = '開発部'), 'データベース担当', '2026-01-05', 'ACTIVE'),
    ('EMP0026', 'テスト社員26', 'employee26@example.invalid', '000-0000-0026', (SELECT id FROM departments WHERE name = '人事部'), '採用担当', '2026-02-02', 'LEAVE'),
    ('EMP0027', 'テスト社員27', 'employee27@example.invalid', '000-0000-0027', (SELECT id FROM departments WHERE name = '営業部'), '営業担当', '2026-03-02', 'ACTIVE'),
    ('EMP0028', 'テスト社員28', 'employee28@example.invalid', '000-0000-0028', (SELECT id FROM departments WHERE name = '総務部'), '総務担当', '2026-04-01', 'ACTIVE'),
    ('EMP0029', 'テスト社員29', 'employee29@example.invalid', '000-0000-0029', (SELECT id FROM departments WHERE name = '経理部'), '会計担当', '2026-05-01', 'ACTIVE'),
    ('EMP0030', 'テスト社員30', 'employee30@example.invalid', '000-0000-0030', (SELECT id FROM departments WHERE name = '情報システム部'), 'IT サポート担当', '2026-06-01', 'RESIGNED');
