select la.application_number AS "Уникальный номер заявления",
       nsiapp.name AS "Вид заявления",
       nsiact.id AS "Идентификатор разрешительного режима",
       nsiact.name AS "Разрешительный режим",
       nsiwork.erul_code AS "Идентификатор разрешительного вида деятельности",
       nsiwork.erul_name AS "Разрешительный вид деятельности",
       la.registration_date AS "Дата подачи заявления",
       nsitype.code AS "Идентификатор разрешительного органа",
       con.full_name AS "Разрешительный орган"
from license.application la,
            nsi.nsi_application_type nsiapp,
            nsi.nsi_activity_kind nsiact,
            nsi.nsi_work_type nsiwork,
            profile.contragent con,
            nsi.nsi_contragent_type nsitype
where la.application_type_id = nsiapp.id
and nsiact.id = la.activity_kind_id
and nsiwork.activity_kind_id = la.activity_kind_id
and la.territory_organ_id = con.id
and nsitype.id = con.contragent_type_id
and la.object_deleted = false;