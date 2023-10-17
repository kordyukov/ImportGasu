select la.input_application_number AS "Уникальный номер заявления",
       nsiapp.name AS "Вид заявления",
       nsiact.id AS "Идентификатор разрешительного режима",
       nsiact.name AS "Разрешительный режим",
       nsiwork.erul_code AS "Идентификатор разрешительного вида деятельности",
       nsiwork.erul_name AS "Разрешительный вид деятельности",
       la.registration_date AS "Дата подачи заявления",
       con.id AS "Идентификатор разрешительного органа",
       con.full_name AS "Разрешительный орган",
       subcode.code AS "Код субъекта РФ",
       subcode.name AS "Субъект РФ",
       appsubmit.name AS "Способ направления",
       contype.name AS "Тип заявителя",
       applicant.full_name AS "Наименование заявителя (для ЮЛ), ФИО для ФЛ и ИП",
       applicant.ogrn AS "ОГРН (ОГРНИП) заявителя (для ЮЛ и ИП)",
       applicant.inn AS "ИНН заявителя",
       b4status.name AS "Решение",
       b4status.name  AS "Причина отказа",
       la.object_edit_date AS "Дата принятия решения",
       la.application_number AS "Регистрационный номер разрешения",
       la.object_edit_date AS "Дата предоставления разрешения",
       la.version_end_date AS "Дата прекращения действия разрешения (при наличии)"
from license.application la
left join nsi.nsi_application_type nsiapp on la.application_type_id = nsiapp.id
left join profile.contragent con on la.territory_organ_id = con.id
left join nsi.nsi_activity_kind nsiact on nsiact.id = la.activity_kind_id
left join nsi.nsi_work_type nsiwork on nsiwork.activity_kind_id = nsiact.id
left join public.b4_fias_address b4addr on con.legal_address_id = b4addr.id
left join nsi.nsi_rf_subjects_codes subcode on b4addr.rf_subjects_codes_id = subcode.id
left join nsi.nsi_application_submit_method appsubmit on la.delivery_method_id = appsubmit.id
left join profile.contragent applicant on applicant.id = la.contragent_id
left join nsi.nsi_contragent_type contype on applicant.contragent_type_id = contype.id
left join public.b4_state b4status on la.state_id = b4status.id
where la.object_deleted = false limit 100;