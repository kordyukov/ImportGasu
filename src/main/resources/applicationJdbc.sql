select distinct on(la.input_application_number) la.input_application_number AS "inputApplicationNumber",
    nsiapp.name AS "applicationType",
    nsiact.id AS "permissiveModeId",
    nsiact.name AS "modeName",
    nsiwork.erul_code AS "typeErulCode",
    nsiwork.erul_name AS "erulName",
    la.registration_date AS "registrationDate",
    con.id AS "permittingAuthorityId",
    con.full_name AS "licensingAuthority",
    sub.code AS "subjectCode",
    sub.name AS "subjectName",
    appsubmit.name AS "referralMethod",
    contype.name AS "applicantType",
    applicant.full_name AS "applicantName",
    applicant.ogrn AS "ogrn",
    applicant.inn AS "inn",
    (select decision.name from nsi.nsi_decision_result decision where dec.decision_result_id = decision.id) AS "decision",
    rej.name  AS "reasonRefusal",
    la.object_edit_date AS "dateDecision",
    la.application_number AS "numberDecision",
    la.object_edit_date AS "dateGrantingPermission",
    la.version_end_date AS "dateTerminationsPermission",
    b4status.name AS "statusDecision"
from license.application la
    left join nsi.nsi_application_type nsiapp on la.application_type_id = nsiapp.id
    left join profile.contragent con on la.territory_organ_id = con.id
    left join nsi.nsi_rf_subjects_codes sub on la.rf_subject_id = sub.id
    left join nsi.nsi_activity_kind nsiact on nsiact.id = la.activity_kind_id
    left join nsi.nsi_work_type nsiwork on nsiwork.activity_kind_id = nsiact.id
    left join nsi.nsi_application_submit_method appsubmit on la.delivery_method_id = appsubmit.id
    left join profile.contragent applicant on applicant.id = la.contragent_id
    left join nsi.nsi_contragent_type contype on applicant.contragent_type_id = contype.id
    left join public.b4_state b4status on la.state_id = b4status.id
    left join license.decision dec on la.id = dec.application_id
    left join license.decision_refusal_reason refreas on refreas.decision_id = dec.id
    left join nsi.nsi_refusal_reason rej on refreas.refusal_reason_id = rej.id
where la.object_deleted = false limit %s offset %s