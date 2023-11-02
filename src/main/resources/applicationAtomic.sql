(
select distinct on(appatom.incoming_number) appatom.incoming_number AS "Уникальный номер заявления",
    nsiappatom.name AS "Вид заявления",
    nsiactatom.code AS "Идентификатор разрешительного режима",
    nsiactatom.name AS "Разрешительный режим",
    nsiworkatom.erul_code AS "Идентификатор разрешительного вида деятельности",
    nsiworkatom.erul_name  AS "Разрешительный вид деятельности",
    appatom.registration_date AS "Дата подачи заявления",
    conatom.rtn_org_code AS "Идентификатор разрешительного органа",
    conatom.full_name AS "Разрешительный орган",
    rfsubjcode.code AS "Код субъекта РФ",
    rfsubjcode.name AS "Субъект РФ",
    appsubmitatom.name AS "Способ направления",
    contypeatom.name AS "Тип заявителя",
    lappatom.full_name AS "Наименование заявителя (для ЮЛ), ФИО для ФЛ и ИП",
    lappatom.ogrn AS "ОГРН (ОГРНИП) заявителя (для ЮЛ и ИП)",
    lappatom.inn AS "ИНН заявителя",
    (select decisionatom.name from public.b4_state decisionatom where decatom.state_id = decisionatom.id) AS "Решение",
    '' AS "Причина отказа",
    decatom.object_create_date AS "Дата принятия решения",
    decatom.decision_number AS "Регистрационный номер разрешения",
    (
     select case when DATE(decatom.approval_date)::varchar is not null then
     concat(SUBSTRING(DATE(decatom.approval_date)::varchar,9,2), '.',
     SUBSTRING(DATE(decatom.approval_date)::varchar,6,2), '.',
     SUBSTRING(DATE(decatom.approval_date)::varchar,1,4)) else '' end
     ) AS "Дата предоставления разрешения",
    decatom.deadline AS "Дата прекращения действия разрешения (при наличии)",
    (select decisionatom.name from public.b4_state decisionatom where decatom.state_id = decisionatom.id) AS "Статус разрешения"
from license_atomic.application_atomic appatom
    left join nsi.nsi_gonernment_services_and_goals nsiappatom on appatom.government_service_id = nsiappatom.id
    left join profile.contragent conatom on appatom.authority_registered_application_id = conatom.id
    left join public.b4_fias_address addrcentr on conatom.legal_address_id = addrcentr.id
    left join nsi.nsi_rf_subjects_codes rfsubjcode on addrcentr.rf_subjects_codes_id = rfsubjcode.id
    left join license_atomic.supervision_direction_activity_kind sdak on appatom.id = sdak.application_id
    left join license_atomic.activity_kind_application_object acao on sdak.id = acao.supervision_direction_activity_kind_id
    left join nsi.nsi_activity_kind nsiactatom on nsiactatom.id = acao.activity_kind_id
    left join nsi.nsi_work_type nsiworkatom on nsiworkatom.activity_kind_id = nsiactatom.id
    left join nsi.nsi_application_submit_method appsubmitatom on appatom.filing_method_id = appsubmitatom.id
    left join license_atomic.applicant_atomic lappatom on appatom.id = lappatom.application_atomic_id
    left join profile.contragent appatomcon on lappatom.contragent_id = appatomcon.id
    left join nsi.nsi_contragent_type contypeatom on appatomcon.contragent_type_id = contypeatom.id
    left join public.b4_state b4status on appatom.state_id = b4status.id
    left join license_atomic.decision_atomic decatom on appatom.id = decatom.application_atomic_id
where appatom.is_deleted = false and lappatom.full_name is not null
  and appatom.registration_date  between %s
    and %s
order by appatom.incoming_number, nsiappatom.name, conatom.full_name
)