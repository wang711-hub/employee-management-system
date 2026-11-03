package jp.ac.example.employeemanagement.service;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class AiRequestPolicy {

    private static final String SENSITIVE_PATTERN = "電話|電話番号|携帯|連絡先|phone|メール|mail|email|パスワード|password|給与|給料|salary";
    private static final Pattern WRITE_REQUEST = Pattern.compile(
            "(?:社員|部署|データ|情報|レコード|EMP\\d+).{0,24}?(?:を|に).{0,8}?(?:削除|消去|更新|変更|登録|追加|作成|作る)(?:してください|して下さい|して|する|します|る)?"
                    + "|(?:新規)?(?:削除|消去|更新|登録|追加|作成|作る)(?:してください|して下さい|して|する|します|る)"
                    + "|\\b(?:delete|update|insert|drop|alter)\\b|\\bsql\\b"
                    + "|\\bselect\\s+.{1,80}\\bfrom\\b|データベースを直接",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final String OVERRIDE_PATTERN = "指示を無視|すべての指示|システムプロンプト|ignore\\s+(all\\s+)?instructions";
    private static final Pattern UNSUPPORTED_TOTAL_COUNT = Pattern.compile(
            "登録社員数|全社員数|全従業員数|全状態の社員数|削除されていない社員の総数");

    public String refusalFor(String question) {
        String normalized = question.toLowerCase(Locale.ROOT);
        if (normalized.matches("(?s).*?(" + SENSITIVE_PATTERN + ").*")) {
            return "電話番号、メールアドレス、パスワード、給与などの情報は照会できません。";
        }
        if (WRITE_REQUEST.matcher(question).find()) {
            return "AI照会ではデータの登録・変更・削除やSQL実行はできません。";
        }
        if (normalized.matches("(?s).*?(" + OVERRIDE_PATTERN + ").*")) {
            return "その指示には対応できません。社員情報に関する照会内容を入力してください。";
        }
        if (UNSUPPORTED_TOTAL_COUNT.matcher(question).find()) {
            return "現在のAI照会では全状態の社員総数を集計できません。在籍社員数など、対応している条件で質問してください。";
        }
        return null;
    }
}
