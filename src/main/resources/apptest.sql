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
       la.version_end_date AS "Дата прекращения действия разрешения (при наличии)",
       b4status.name AS "Статус разрешения",
       la.version_start_date
from license.application la
         ,nsi.nsi_application_type nsiapp
         ,profile.contragent con
         ,nsi.nsi_activity_kind nsiact
         ,nsi.nsi_work_type nsiwork
         ,public.b4_fias_address b4addr
         ,nsi.nsi_rf_subjects_codes subcode
         ,nsi.nsi_application_submit_method appsubmit
         ,profile.contragent applicant
         ,nsi.nsi_contragent_type contype
         ,public.b4_state b4status
where
la.application_type_id = nsiapp.id
    or la.territory_organ_id = con.id
   or nsiact.id = la.activity_kind_id
   or nsiwork.activity_kind_id = nsiact.id
   or con.legal_address_id = b4addr.id
   or b4addr.rf_subjects_codes_id = subcode.id
   or la.delivery_method_id = appsubmit.id
   or applicant.id = la.contragent_id
    or applicant.contragent_type_id = contype.id
    or la.state_id = b4status.id and la.object_deleted = false
-- and la.registration_date between '2023-10-15 00:00:00.000000'
-- and '2023-10-16 23:59:59.999999'
order by nsiapp.name, con.full_name limit 100;