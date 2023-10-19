WITH sum AS (select distinct on(la.input_application_number) la.input_application_number
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
    )

select count(sum.input_application_number) from sum;