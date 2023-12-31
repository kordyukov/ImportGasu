select distinct
on(la.input_application_number) la.input_application_number AS "Уникальный номер заявления",
    nsiapp.name AS "Вид заявления",
    nsiact.erul_code AS "Идентификатор лицензируемого вида деятельности",
    nsiact.name AS "Наименование лицензируемого вида деятельности",
    la.registration_date AS "Дата подачи заявления",
--     decreturn.decision_return_date  AS "Дата уведомления заявителя о необходимости устранения",
--     ara.object_create_date AS "Дата принятия решения лицензирующим органом о рассмотрении",
    ''  AS "Дата уведомления заявителя о необходимости устранения",
    '' AS "Дата принятия решения лицензирующим органом о рассмотрении",
    '00109' AS "Идентификатор разрешительного органа",
    con.full_name AS "Наименование лицензирующего органа",
    (
    select case when nsisub.code is not null
     then nsisub.code else
        (select case when sub.code is not null then sub.code
          else (select case when rfsappcon.code is not null then rfsappcon.code
              else (select case when fedappcon.code is not null then fedappcon.code
                         else (select case when laaddrf.code is not null then laaddrf.code
                             else '00' end ) end ) end ) end ) end
     ) AS "Код субъекта РФ",
    (
     select case when nsisub.name is not null
      then nsisub.name else
        (select case when sub.name is not null then sub.name
           else (select case when rfsappcon.name is not null then rfsappcon.name
               else (select case when fedappcon.name is not null then fedappcon.name
                    else (select case when laaddrf.name is not null then laaddrf.name
                        else 'Российская Федерация' end) end ) end ) end )  end
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
    ord.approval_date AS "Дата принятия решения",
    (select case when license.license_number is not null
        then license.license_number
        else (select case when license.temp_license_number is not null then license.temp_license_number
              else (select case when licdec.temp_license_number is not null then licdec.temp_license_number
                  else (select case when licdec.license_number is not null then licdec.license_number
                  else (select case when licterm.terminated_license_number is not null
                      then licterm.terminated_license_number
                           else
                               licterm.terminated_temp_license_number end )  end ) end ) end ) end ) AS "Регистрационный номер лицензии"
from license.application la
    left join nsi.nsi_application_type nsiapp
on la.application_type_id = nsiapp.id
    left join public.b4_state status on la.state_id = status.id
    left join profile.contragent con on la.territory_organ_id = con.id
    left join nsi.nsi_rf_subjects_codes sub on la.rf_subject_id = sub.id
    left join nsi.nsi_activity_kind nsiact on nsiact.id = la.activity_kind_id
    left join license.applicant lapp on la.id = lapp.application_id
    left join nsi.nsi_contragent_type contype on lapp.applicant_type_id = contype.id
    left join license.decision dec on la.id = dec.application_id
    left join license.field_inspection isp on dec.id = isp.decision_id
    left join license.decision_refusal_reason refreas on refreas.decision_id = dec.id
    left join nsi.nsi_refusal_reason rej on refreas.refusal_reason_id = rej.id
    left join license.license_for_application licforapp on licforapp.application_id = la.id
    left join license.license license on licforapp.license_id = license.id
    left join license.license_address laaddrappcon on license.id = laaddrappcon.license_id
    left join license.license licdec on dec.id = licdec.decision_id
    left join public.b4_fias_address b4fiasid on laaddrappcon.fias_id = b4fiasid.id
    left join license.application_review_acceptance ara on la.id = ara.application_id
    left join public.b4_fias_address b4code on lapp.legal_address_id = b4code.id
    left join nsi.nsi_rf_subjects_codes nsisub on b4code.rf_subjects_codes_id = nsisub.id
    left join profile.contragent appcon on appcon.id = lapp.contragent_id
    left join public.b4_fias_address beappcon on appcon.legal_address_id = beappcon.id
    left join nsi.nsi_rf_subjects_codes rfsappcon on beappcon.rf_subjects_codes_id = rfsappcon.id
    left join nsi.nsi_rf_subjects_codes fedappcon on appcon.fed_subject_code_id = fedappcon.id
    left join nsi.nsi_rf_subjects_codes laaddrf on b4fiasid.rf_subjects_codes_id = laaddrf.id
    left join license.decision_return decreturn on decreturn.application_id = la.id
    left join license.license_for_termination licterm on la.id = licterm.application_id
    left join license.order_for_decision orddec on dec.id = orddec.decision_id
    left join license.order ord on orddec.order_id = ord.id
where la.object_deleted = false and status.name <> 'Удалено' and status.name <> 'Рассмотрение прекращено'
          and ord.approval_date is not null
  and la.registration_date between %s
  and %s
order by la.input_application_number, la.registration_date, nsiapp.name, con.full_name

