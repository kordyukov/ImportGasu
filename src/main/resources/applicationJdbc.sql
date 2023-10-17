select la.input_application_number AS "inputApplicationNumber",
       nsiapp.name,
       nsiact.id AS "permissiveModeId",
       nsiact.name AS "modeName",
       nsiwork.erul_code AS "typeErulCode",
       nsiwork.erul_name AS "erulName",
       la.registration_date,
       con.id AS "contragentId",
       con.full_name AS "contragentName",
       subcode.code AS "subjectCode",
       subcode.name AS "subjectName",
       appsubmit.name AS "referralMethod",
       contype.name AS "contragentType",
       applicant.full_name,
       applicant.ogrn,
       applicant.inn,
       b4status.name AS "decision",
       b4status.name AS "reasonRefusal",
       la.object_edit_date AS "dateDecision",
       la.application_number AS "numberDecision",
       la.object_edit_date AS "dateGrantingPermission",
       la.version_end_date AS "dateTerminationsPermission",
       b4status.name AS "statusDecision"
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
where la.object_deleted = false;