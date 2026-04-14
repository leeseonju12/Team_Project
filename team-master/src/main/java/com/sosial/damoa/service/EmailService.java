package com.sosial.damoa.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendReplyToUser(String toEmail, String inquiryTitle, String replyContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[소셜다모아] 문의 답변 안내");

            String html = """
                <div style="margin:0;padding:32px 0;background:linear-gradient(180deg,#f8fafc 0%%,#eef2ff 100%%);font-family:'Apple SD Gothic Neo','Malgun Gothic',Arial,sans-serif;color:#0f172a;">
                  <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="border-collapse:collapse;">
                    <tr>
                      <td align="center">
                        <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="640" style="width:640px;max-width:640px;background:#ffffff;border:1px solid #e5e7eb;border-radius:20px;overflow:hidden;box-shadow:0 18px 40px rgba(15,23,42,0.08);">

                          <tr>
                            <td style="padding:0;">
                              <div style="height:6px;background:linear-gradient(90deg,#14b8a6 0%%,#6366f1 100%%);"></div>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:24px 28px 18px 28px;background:#ffffff;">
                              <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%">
                                <tr>
                                  <td align="left" valign="middle">
                                    <div style="display:inline-block;padding:8px 12px;border-radius:999px;background:#eef2ff;color:#4f46e5;font-size:12px;font-weight:700;letter-spacing:-0.1px;">
                                      소셜다모아
                                    </div>
                                    <div style="margin-top:14px;font-size:26px;font-weight:800;line-height:1.4;letter-spacing:-0.4px;color:#111827;">
                                      문의하신 내용에<br>
                                      답변이 등록되었습니다
                                    </div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:0 28px 10px 28px;">
                              <div style="font-size:15px;line-height:1.9;color:#475569;">
                                안녕하세요.<br>
                                소셜다모아 문의 시스템을 통해 접수하신 문의에 대한 답변을 아래와 같이 안내드립니다.
                              </div>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:18px 28px 10px 28px;">
                              <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="border-collapse:collapse;background:linear-gradient(180deg,#f8fafc 0%%,#ffffff 100%%);border:1px solid #e5e7eb;border-radius:16px;">
                                <tr>
                                  <td style="padding:18px 20px;">
                                    <div style="font-size:12px;font-weight:700;color:#64748b;margin-bottom:8px;letter-spacing:0.2px;">문의 제목</div>
                                    <div style="font-size:16px;font-weight:700;line-height:1.7;color:#111827;">%s</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:12px 28px 12px 28px;">
                              <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%" style="border-collapse:collapse;background:#ffffff;border:1px solid #e5e7eb;border-radius:16px;">
                                <tr>
                                  <td style="padding:18px 20px;">
                                    <div style="font-size:12px;font-weight:700;color:#64748b;margin-bottom:10px;letter-spacing:0.2px;">답변 내용</div>
                                    <div style="font-size:15px;line-height:1.95;color:#334155;white-space:pre-wrap;">%s</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:18px 28px 28px 28px;">
                              <div style="padding:16px 18px;border-radius:14px;background:#f8fafc;border:1px dashed #cbd5e1;">
                                <div style="font-size:13px;line-height:1.8;color:#64748b;">
                                  추가 문의가 있으시면 언제든 다시 문의해 주세요.<br>
                                  빠르게 확인 후 안내드리겠습니다.
                                </div>
                              </div>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:18px 28px;background:#f8fafc;border-top:1px solid #e5e7eb;">
                              <div style="font-size:12px;line-height:1.8;color:#94a3b8;">
                                본 메일은 소셜다모아 문의 시스템에서 자동 발송되었습니다.<br>
                                © 2026 소셜다모아. All rights reserved.
                              </div>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </div>
                """.formatted(
                    escapeHtml(inquiryTitle),
                    escapeHtml(replyContent)
            );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("메일 발송 실패", e);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
    }
}