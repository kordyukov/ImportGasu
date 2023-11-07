select distinct
on(la.input_application_number) la.input_application_number AS "Уникальный номер заявления",
    nsiapp.name AS "Вид заявления",
    nsiact.erul_code AS "Идентификатор лицензируемого вида деятельности",
    nsiact.name AS "Наименование лицензируемого вида деятельности",
    la.registration_date AS "Дата подачи заявления",
    '' AS "Дата уведомления заявителя о необходимости устранения",
    '' AS "Дата принятия решения лицензирующим органом о рассмотрении",
    con.exploit_number AS "Идентификатор разрешительного органа",
    con.full_name AS "Наименование лицензирующего органа",
    (SELECT CASE WHEN sub.code is null THEN la.rf_subject_id:: varchar ELSE sub.code END) AS "Код субъекта РФ",
    (SELECT CASE WHEN sub.name is null THEN
    (
    select nsisub.name from nsi.nsi_rf_subjects_codes nsisub where la.rf_subject_id = nsisub.id
    )
    ELSE sub.name END
    ) AS "Субъект РФ",
    (select case when la.epgu_number is not null then 'Да' else 'Нет' end) AS "Направлено через ЕПГУ (Да/Нет)",
    (select case when contype.name is not null then contype.name
     else (select nat.name from nsi.nsi_applicant_type nat where lapp.applicant_type_id = nat.id) end ) AS "Тип заявителя",
    (select case when lapp.full_name is not null then lapp.full_name
     else concat(lapp.fam, ' ', lapp.name, ' ', lapp.fname) end)  AS "Наименование заявителя (для ЮЛ), ФИО для ФЛ и ИП",
    lapp.ogrn AS "ОГРН (ОГРНИП) заявителя (для ЮЛ и ИП)",
    lapp.inn AS "ИНН заявителя",
    (select case when isp.id is not null
     then 'Выездная' else 'Дистанционная' end ) AS "Способ проверки",
    (select case when isp.id is not null then isp.object_create_date
     else ara.object_create_date end) AS "Дата проведения проверки",
    (select decision.name from nsi.nsi_decision_result decision where dec.decision_result_id = decision.id) AS "Решение",
    rej.name AS "Причина отказа",
    dec.object_create_date AS "Дата принятия решения",
    license.temp_license_number AS "Регистрационный номер лицензии"
from license.application la
    left join nsi.nsi_application_type nsiapp
on la.application_type_id = nsiapp.id
    left join profile.contragent con on la.territory_organ_id = con.id
    left join nsi.nsi_rf_subjects_codes sub on la.rf_subject_id = sub.id
    left join nsi.nsi_activity_kind nsiact on nsiact.id = la.activity_kind_id
    left join license.applicant lapp on la.id = lapp.application_id
    left join nsi.nsi_contragent_type contype on lapp.applicant_type_id = contype.id
    left join license.decision dec on la.id = dec.application_id
    left join license.decision_refusal_reason refreas on refreas.decision_id = dec.id
    left join nsi.nsi_refusal_reason rej on refreas.refusal_reason_id = rej.id
    left join license.license license on dec.id = license.decision_id
    left join license.field_inspection isp on dec.id = isp.decision_id
    left join license.application_review_acceptance ara on la.id = ara.application_id
where la.object_deleted = false limit 100
  and la.registration_date between %s
  and %s
order by la.input_application_number, nsiapp.name, con.full_name

