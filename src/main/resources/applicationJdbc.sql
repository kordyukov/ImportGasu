select distinct
on(la.input_application_number) la.input_application_number AS "Уникальный номер заявления",
    nsiapp.name AS "Вид заявления",
    nsiact.erul_code AS "Идентификатор разрешительного режима",
    nsiact.name AS "Разрешительный режим",
    nsiwork.erul_code AS "Идентификатор разрешительного вида деятельности",
    nsiwork.erul_name AS "Разрешительный вид деятельности",
    la.registration_date AS "Дата подачи заявления",
    con.rtn_org_code AS "Идентификатор разрешительного органа",
    con.full_name AS "Разрешительный орган",
    (SELECT CASE WHEN sub.code is null THEN la.rf_subject_id:: varchar ELSE sub.code END) AS "Код субъекта РФ",
    (SELECT CASE WHEN sub.name is null THEN
    (
    select nsisub.name from nsi.nsi_rf_subjects_codes nsisub where la.rf_subject_id = nsisub.id
    )
    ELSE sub.name END
    ) AS "Субъект РФ",
    appsubmit.name AS "Способ направления",
    contype.name AS "Тип заявителя",
    lapp.full_name  AS "Наименование заявителя (для ЮЛ), ФИО для ФЛ и ИП",
    lapp.ogrn AS "ОГРН (ОГРНИП) заявителя (для ЮЛ и ИП)",
    lapp.inn AS "ИНН заявителя",
    (select decision.name from nsi.nsi_decision_result decision where dec.decision_result_id = decision.id) AS "Решение",
    rej.name AS "Причина отказа",
    dec.object_create_date AS "Дата принятия решения",
    dec.decision_number AS "Регистрационный номер разрешения",
    (
        select case when dec.decision_date is not null then
        concat(SUBSTRING(dec.decision_date::varchar,9,2), '.',
        SUBSTRING(dec.decision_date::varchar,6,2), '.',
        SUBSTRING(dec.decision_date::varchar,1,4)) else '' end
    ) AS "Дата предоставления разрешения",
    dec.version_end_date AS "Дата прекращения действия разрешения (при наличии)",
    (select st.name from public.b4_state st where dec.status_id = st.id) AS "Статус разрешения"
from license.application la
    left join nsi.nsi_application_type nsiapp
on la.application_type_id = nsiapp.id
    left join profile.contragent con on la.territory_organ_id = con.id
    left join nsi.nsi_rf_subjects_codes sub on la.rf_subject_id = sub.id
    left join nsi.nsi_activity_kind nsiact on nsiact.id = la.activity_kind_id
    left join nsi.nsi_work_type nsiwork on nsiwork.activity_kind_id = nsiact.id
    left join nsi.nsi_application_submit_method appsubmit on la.delivery_method_id = appsubmit.id
    left join license.applicant lapp on la.id = lapp.application_id
    left join nsi.nsi_contragent_type contype on lapp.applicant_type_id = contype.id
    left join public.b4_state b4status on la.state_id = b4status.id
    left join license.decision dec on la.id = dec.application_id
    left join license.decision_refusal_reason refreas on refreas.decision_id = dec.id
    left join nsi.nsi_refusal_reason rej on refreas.refusal_reason_id = rej.id
where la.object_deleted = false
  and la.registration_date between %s
  and %s
order by la.input_application_number, nsiapp.name, con.full_name