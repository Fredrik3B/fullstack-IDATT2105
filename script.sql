USE app_db;

SET @org_id  = UNHEX('b3f897f835c7474f8a0c08f8abc9ca61');
SET @kari_id = UNHEX('f80cbd1141e34c7f8df01c637ef32cd0');
SET @tore_id = UNHEX('57ac8f8d5d7747b99576a5e20eb5af92');

INSERT IGNORE INTO organizations (id, name, join_code)
VALUES (UNHEX('b3f897f835c7474f8a0c08f8abc9ca61'), 'Everest Sushi & Fusion', 'EVR-0011');

INSERT IGNORE INTO users (id, email, first_name, last_name, password, organization_id)
VALUES 
(UNHEX('f80cbd1141e34c7f8df01c637ef32cd0'), 'kari@demo.no', 'Kari', 'Nordmann', '$2a$10$placeholder', @org_id),
(UNHEX('57ac8f8d5d7747b99576a5e20eb5af92'), 'tore@demo.no', 'Tore', 'Hansen', '$2a$10$placeholder', @org_id);


START TRANSACTION;

-- =========================================================
-- 1. TEMPERATURE ZONES
-- =========================================================

INSERT INTO temperature_zones (name, zone_type, compliance_area, target_min, target_max, organization_id)
VALUES
('Main Fridge', 'FRIDGE', 'IK_MAT', 2.00, 4.00, @org_id),
('Prep Fridge', 'FRIDGE', 'IK_MAT', 2.00, 4.00, @org_id),
('Freezer Room', 'FREEZER', 'IK_MAT', -22.00, -18.00, @org_id),
('Cold Storage', 'COLD_STORAGE', 'IK_MAT', 0.00, 4.00, @org_id),
('Receiving Area', 'RECEIVING', 'IK_MAT', 0.00, 8.00, @org_id);

SET @zone_main_fridge = (
  SELECT id FROM temperature_zones
  WHERE organization_id = @org_id AND name = 'Main Fridge'
  ORDER BY id DESC LIMIT 1
);
SET @zone_prep_fridge = (
  SELECT id FROM temperature_zones
  WHERE organization_id = @org_id AND name = 'Prep Fridge'
  ORDER BY id DESC LIMIT 1
);
SET @zone_freezer = (
  SELECT id FROM temperature_zones
  WHERE organization_id = @org_id AND name = 'Freezer Room'
  ORDER BY id DESC LIMIT 1
);
SET @zone_cold_storage = (
  SELECT id FROM temperature_zones
  WHERE organization_id = @org_id AND name = 'Cold Storage'
  ORDER BY id DESC LIMIT 1
);
SET @zone_receiving = (
  SELECT id FROM temperature_zones
  WHERE organization_id = @org_id AND name = 'Receiving Area'
  ORDER BY id DESC LIMIT 1
);

-- =========================================================
-- 2. FOOD TASK TEMPLATES (25)
-- =========================================================

INSERT INTO template_tasks
(title, section_type, compliance_area, unit, meta, target_min, target_max, organisation_id, temperature_zone_id)
VALUES
('Check main fridge temperature', 'TEMPERATURE_CONTROL', 'IK_MAT', 'C', 'daily-temp', 2.00, 4.00, @org_id, @zone_main_fridge),
('Check prep fridge temperature', 'TEMPERATURE_CONTROL', 'IK_MAT', 'C', 'daily-temp', 2.00, 4.00, @org_id, @zone_prep_fridge),
('Check freezer room temperature', 'TEMPERATURE_CONTROL', 'IK_MAT', 'C', 'daily-temp', -22.00, -18.00, @org_id, @zone_freezer),
('Check cold storage temperature', 'TEMPERATURE_CONTROL', 'IK_MAT', 'C', 'daily-temp', 0.00, 4.00, @org_id, @zone_cold_storage),
('Check delivery temperature on receiving', 'TEMPERATURE_CONTROL', 'IK_MAT', 'C', 'delivery-temp', 0.00, 8.00, @org_id, @zone_receiving),

('Wash and sanitize cutting boards', 'CLEANING_SANITATION', 'IK_MAT', NULL, 'cleaning', NULL, NULL, @org_id, NULL),
('Sanitize prep surfaces', 'CLEANING_SANITATION', 'IK_MAT', NULL, 'cleaning', NULL, NULL, @org_id, NULL),
('Clean sinks and drain areas', 'CLEANING_SANITATION', 'IK_MAT', NULL, 'cleaning', NULL, NULL, @org_id, NULL),
('Empty food waste containers', 'CLEANING_SANITATION', 'IK_MAT', NULL, 'cleaning', NULL, NULL, @org_id, NULL),
('Check cleaning chemical labels', 'CLEANING_SANITATION', 'IK_MAT', NULL, 'safety', NULL, NULL, @org_id, NULL),

('Verify handwash station supplies', 'HYGIENE', 'IK_MAT', NULL, 'hygiene', NULL, NULL, @org_id, NULL),
('Ensure staff use gloves correctly', 'HYGIENE', 'IK_MAT', NULL, 'hygiene', NULL, NULL, @org_id, NULL),
('Check personal hygiene compliance', 'HYGIENE', 'IK_MAT', NULL, 'hygiene', NULL, NULL, @org_id, NULL),
('Confirm allergen area separation', 'HYGIENE', 'IK_MAT', NULL, 'allergen', NULL, NULL, @org_id, NULL),
('Inspect sanitizer concentration logs', 'HYGIENE', 'IK_MAT', NULL, 'hygiene', NULL, NULL, @org_id, NULL),

('Verify date labels on prepared food', 'FOOD_STORAGE', 'IK_MAT', NULL, 'storage', NULL, NULL, @org_id, NULL),
('Check FIFO in fridge storage', 'FOOD_STORAGE', 'IK_MAT', NULL, 'storage', NULL, NULL, @org_id, NULL),
('Check dry storage organization', 'FOOD_STORAGE', 'IK_MAT', NULL, 'storage', NULL, NULL, @org_id, NULL),
('Inspect packaging integrity', 'FOOD_STORAGE', 'IK_MAT', NULL, 'storage', NULL, NULL, @org_id, NULL),
('Verify raw and ready-to-eat separation', 'FOOD_STORAGE', 'IK_MAT', NULL, 'storage', NULL, NULL, @org_id, NULL),

('Opening kitchen hygiene check', 'OPENING_ROUTINE', 'IK_MAT', NULL, 'opening', NULL, NULL, @org_id, NULL),
('Closing kitchen hygiene check', 'CLOSING_ROUTINE', 'IK_MAT', NULL, 'closing', NULL, NULL, @org_id, NULL),
('Check pest control log', 'SAFETY_COMPLIANCE', 'IK_MAT', NULL, 'compliance', NULL, NULL, @org_id, NULL),
('Review HACCP critical points', 'SAFETY_COMPLIANCE', 'IK_MAT', NULL, 'compliance', NULL, NULL, @org_id, NULL),
('Verify emergency contact sheet is updated', 'SAFETY_COMPLIANCE', 'IK_MAT', NULL, 'compliance', NULL, NULL, @org_id, NULL);

-- =========================================================
-- 3. ALCOHOL TASK TEMPLATES (25)
-- =========================================================

INSERT INTO template_tasks
(title, section_type, compliance_area, unit, meta, target_min, target_max, organisation_id, temperature_zone_id)
VALUES
('Check age verification signage', 'SAFETY_COMPLIANCE', 'IK_ALKOHOL', NULL, 'alcohol', NULL, NULL, @org_id, NULL),
('Verify ID check routine with staff', 'SAFETY_COMPLIANCE', 'IK_ALKOHOL', NULL, 'alcohol', NULL, NULL, @org_id, NULL),
('Confirm refusal routine for intoxicated guests', 'SAFETY_COMPLIANCE', 'IK_ALKOHOL', NULL, 'alcohol', NULL, NULL, @org_id, NULL),
('Check incident log is updated', 'SAFETY_COMPLIANCE', 'IK_ALKOHOL', NULL, 'alcohol', NULL, NULL, @org_id, NULL),
('Review alcohol law reminders with shift team', 'SAFETY_COMPLIANCE', 'IK_ALKOHOL', NULL, 'alcohol', NULL, NULL, @org_id, NULL),

('Check bar opening stock control', 'OPENING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-open', NULL, NULL, @org_id, NULL),
('Verify spirits cabinet lock status', 'OPENING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-open', NULL, NULL, @org_id, NULL),
('Check tap line cleanliness', 'OPENING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-open', NULL, NULL, @org_id, NULL),
('Confirm register and till match opening count', 'OPENING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-open', NULL, NULL, @org_id, NULL),
('Check bar area cleanliness before opening', 'OPENING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-open', NULL, NULL, @org_id, NULL),

('Check bar closing stock control', 'CLOSING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-close', NULL, NULL, @org_id, NULL),
('Secure alcohol storage at closing', 'CLOSING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-close', NULL, NULL, @org_id, NULL),
('Log refused alcohol sales', 'CLOSING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-close', NULL, NULL, @org_id, NULL),
('Review incident notes before end of shift', 'CLOSING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-close', NULL, NULL, @org_id, NULL),
('Check CCTV/event log if needed', 'CLOSING_ROUTINE', 'IK_ALKOHOL', NULL, 'bar-close', NULL, NULL, @org_id, NULL),

('Verify responsible serving training status', 'SERVICE_QUALITY', 'IK_ALKOHOL', NULL, 'training', NULL, NULL, @org_id, NULL),
('Confirm guest service standards at bar', 'SERVICE_QUALITY', 'IK_ALKOHOL', NULL, 'service', NULL, NULL, @org_id, NULL),
('Check menu alcohol information is visible', 'SERVICE_QUALITY', 'IK_ALKOHOL', NULL, 'service', NULL, NULL, @org_id, NULL),
('Review shift communication on alcohol policies', 'SERVICE_QUALITY', 'IK_ALKOHOL', NULL, 'service', NULL, NULL, @org_id, NULL),
('Confirm manager-on-duty availability', 'SERVICE_QUALITY', 'IK_ALKOHOL', NULL, 'service', NULL, NULL, @org_id, NULL),

('Inspect bar hygiene and glass handling', 'HYGIENE', 'IK_ALKOHOL', NULL, 'bar-hygiene', NULL, NULL, @org_id, NULL),
('Check cleaning of beer taps and nozzles', 'CLEANING_SANITATION', 'IK_ALKOHOL', NULL, 'bar-cleaning', NULL, NULL, @org_id, NULL),
('Check storage of open alcohol containers', 'FOOD_STORAGE', 'IK_ALKOHOL', NULL, 'bar-storage', NULL, NULL, @org_id, NULL),
('Review weekly alcohol deviation summary', 'SAFETY_COMPLIANCE', 'IK_ALKOHOL', NULL, 'weekly', NULL, NULL, @org_id, NULL),
('Confirm compliance binder is updated', 'SAFETY_COMPLIANCE', 'IK_ALKOHOL', NULL, 'weekly', NULL, NULL, @org_id, NULL);

-- =========================================================
-- 4. CHECKLISTS: DAILY, WEEKLY, MONTHLY
-- =========================================================

SET @today_daily_key = DATE_FORMAT(CURDATE(), '%Y-%m-%d');
SET @today_weekly_key = CONCAT(YEAR(CURDATE()), '-W', LPAD(WEEK(CURDATE(), 3), 2, '0'));
SET @today_monthly_key = DATE_FORMAT(CURDATE(), '%Y-%m');

INSERT INTO checklists
(name, description, frequency, compliance_area, active_period_key, recurring, displayed_on_workbench, active, organization_id)
VALUES
('Daily Kitchen Opening', 'Opening checks for kitchen hygiene, storage and temperatures', 'DAILY', 'IK_MAT', @today_daily_key, 1, 1, 1, @org_id),
('Daily Kitchen Closing', 'Closing checks for kitchen cleaning and compliance', 'DAILY', 'IK_MAT', @today_daily_key, 1, 1, 1, @org_id),
('Weekly Food Safety Review', 'Weekly review of food safety routines and storage', 'WEEKLY', 'IK_MAT', @today_weekly_key, 1, 1, 1, @org_id),
('Monthly IK-Mat Audit', 'Monthly internal food compliance review', 'MONTHLY', 'IK_MAT', @today_monthly_key, 1, 1, 1, @org_id),

('Daily Bar Opening', 'Opening checks for bar operations and alcohol compliance', 'DAILY', 'IK_ALKOHOL', @today_daily_key, 1, 1, 1, @org_id),
('Daily Bar Closing', 'Closing checks for bar operations and alcohol compliance', 'DAILY', 'IK_ALKOHOL', @today_daily_key, 1, 1, 1, @org_id),
('Weekly Alcohol Compliance Review', 'Weekly review of serving routines and incident handling', 'WEEKLY', 'IK_ALKOHOL', @today_weekly_key, 1, 1, 1, @org_id),
('Monthly IK-Alkohol Audit', 'Monthly internal alcohol compliance review', 'MONTHLY', 'IK_ALKOHOL', @today_monthly_key, 1, 1, 1, @org_id);

SET @cl_food_open = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Daily Kitchen Opening' ORDER BY id DESC LIMIT 1);
SET @cl_food_close = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Daily Kitchen Closing' ORDER BY id DESC LIMIT 1);
SET @cl_food_weekly = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Weekly Food Safety Review' ORDER BY id DESC LIMIT 1);
SET @cl_food_monthly = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Monthly IK-Mat Audit' ORDER BY id DESC LIMIT 1);

SET @cl_alc_open = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Daily Bar Opening' ORDER BY id DESC LIMIT 1);
SET @cl_alc_close = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Daily Bar Closing' ORDER BY id DESC LIMIT 1);
SET @cl_alc_weekly = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Weekly Alcohol Compliance Review' ORDER BY id DESC LIMIT 1);
SET @cl_alc_monthly = (SELECT id FROM checklists WHERE organization_id = @org_id AND name = 'Monthly IK-Alkohol Audit' ORDER BY id DESC LIMIT 1);

-- =========================================================
-- 5. LINK TEMPLATES TO CHECKLISTS
-- =========================================================

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_food_open, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_MAT'
  AND title IN (
    'Check main fridge temperature',
    'Check prep fridge temperature',
    'Check freezer room temperature',
    'Check cold storage temperature',
    'Check delivery temperature on receiving',
    'Verify handwash station supplies',
    'Check personal hygiene compliance',
    'Verify date labels on prepared food',
    'Check FIFO in fridge storage',
    'Opening kitchen hygiene check'
  );

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_food_close, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_MAT'
  AND title IN (
    'Wash and sanitize cutting boards',
    'Sanitize prep surfaces',
    'Clean sinks and drain areas',
    'Empty food waste containers',
    'Check cleaning chemical labels',
    'Ensure staff use gloves correctly',
    'Inspect sanitizer concentration logs',
    'Check dry storage organization',
    'Inspect packaging integrity',
    'Closing kitchen hygiene check'
  );

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_food_weekly, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_MAT'
  AND title IN (
    'Confirm allergen area separation',
    'Verify raw and ready-to-eat separation',
    'Check pest control log',
    'Review HACCP critical points',
    'Verify emergency contact sheet is updated'
  );

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_food_monthly, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_MAT'
  AND title IN (
    'Check main fridge temperature',
    'Check freezer room temperature',
    'Check delivery temperature on receiving',
    'Review HACCP critical points',
    'Check pest control log'
  );

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_alc_open, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_ALKOHOL'
  AND title IN (
    'Check age verification signage',
    'Verify ID check routine with staff',
    'Check bar opening stock control',
    'Verify spirits cabinet lock status',
    'Check tap line cleanliness',
    'Confirm register and till match opening count',
    'Check bar area cleanliness before opening',
    'Confirm manager-on-duty availability',
    'Confirm guest service standards at bar',
    'Check menu alcohol information is visible'
  );

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_alc_close, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_ALKOHOL'
  AND title IN (
    'Confirm refusal routine for intoxicated guests',
    'Check incident log is updated',
    'Review alcohol law reminders with shift team',
    'Check bar closing stock control',
    'Secure alcohol storage at closing',
    'Log refused alcohol sales',
    'Review incident notes before end of shift',
    'Check CCTV/event log if needed',
    'Inspect bar hygiene and glass handling',
    'Check cleaning of beer taps and nozzles'
  );

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_alc_weekly, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_ALKOHOL'
  AND title IN (
    'Review weekly alcohol deviation summary',
    'Confirm compliance binder is updated',
    'Verify responsible serving training status',
    'Review shift communication on alcohol policies',
    'Check storage of open alcohol containers'
  );

INSERT INTO checklist_task_templates (checklist_id, task_template_id)
SELECT @cl_alc_monthly, id
FROM template_tasks
WHERE organisation_id = @org_id
  AND compliance_area = 'IK_ALKOHOL'
  AND title IN (
    'Check age verification signage',
    'Confirm refusal routine for intoxicated guests',
    'Check incident log is updated',
    'Review weekly alcohol deviation summary',
    'Confirm compliance binder is updated'
  );

-- =========================================================
-- 6. PERIOD TABLES
-- =========================================================

DROP TEMPORARY TABLE IF EXISTS demo_days;
CREATE TEMPORARY TABLE demo_days (
  day_date DATE NOT NULL,
  period_key VARCHAR(16) NOT NULL
);

INSERT INTO demo_days (day_date, period_key)
WITH RECURSIVE d AS (
  SELECT CURDATE() - INTERVAL 13 DAY AS day_date
  UNION ALL
  SELECT day_date + INTERVAL 1 DAY
  FROM d
  WHERE day_date < CURDATE()
)
SELECT day_date, DATE_FORMAT(day_date, '%Y-%m-%d')
FROM d;

DROP TEMPORARY TABLE IF EXISTS demo_weeks;
CREATE TEMPORARY TABLE demo_weeks (
  week_start DATE NOT NULL,
  period_key VARCHAR(16) NOT NULL
);

INSERT INTO demo_weeks (week_start, period_key)
SELECT DISTINCT
  DATE_SUB(day_date, INTERVAL WEEKDAY(day_date) DAY) AS week_start,
  CONCAT(YEAR(day_date), '-W', LPAD(WEEK(day_date, 3), 2, '0')) AS period_key
FROM demo_days
ORDER BY week_start;

DROP TEMPORARY TABLE IF EXISTS demo_months;
CREATE TEMPORARY TABLE demo_months (
  month_start DATE NOT NULL,
  period_key VARCHAR(16) NOT NULL
);

INSERT INTO demo_months (month_start, period_key)
SELECT DISTINCT
  DATE_SUB(day_date, INTERVAL DAYOFMONTH(day_date) - 1 DAY) AS month_start,
  DATE_FORMAT(day_date, '%Y-%m') AS period_key
FROM demo_days
ORDER BY month_start;

-- =========================================================
-- 7. DAILY ACTIVATED TASKS (14 days)
-- =========================================================

INSERT INTO activated_tasks
(completed, flagged, active, meta, period_key, ended_at, checklist_id, task_template_id)
SELECT
  CASE
    WHEN t.title IN ('Check prep fridge temperature', 'Log refused alcohol sales')
         AND DAYOFMONTH(d.day_date) IN (3, 8) THEN 0
    ELSE 1
  END AS completed,
  CASE
    WHEN t.title IN ('Check prep fridge temperature', 'Log refused alcohol sales', 'Check cleaning chemical labels')
         AND DAYOFMONTH(d.day_date) IN (3, 8, 11) THEN 1
    ELSE 0
  END AS flagged,
  0 AS active,
  'daily-demo' AS meta,
  d.period_key,
  TIMESTAMP(d.day_date, '21:00:00'),
  ctt.checklist_id,
  ctt.task_template_id
FROM checklist_task_templates ctt
JOIN template_tasks t ON t.id = ctt.task_template_id
JOIN demo_days d
WHERE ctt.checklist_id IN (@cl_food_open, @cl_food_close, @cl_alc_open, @cl_alc_close);

-- =========================================================
-- 8. WEEKLY ACTIVATED TASKS (2 recent weeks)
-- =========================================================

INSERT INTO activated_tasks
(completed, flagged, active, meta, period_key, ended_at, checklist_id, task_template_id)
SELECT
  CASE
    WHEN t.title IN ('Review weekly alcohol deviation summary', 'Check pest control log')
         AND WEEK(w.week_start, 3) = WEEK(CURDATE() - INTERVAL 7 DAY, 3) THEN 0
    ELSE 1
  END AS completed,
  CASE
    WHEN t.title IN ('Review weekly alcohol deviation summary', 'Check pest control log')
         AND WEEK(w.week_start, 3) = WEEK(CURDATE() - INTERVAL 7 DAY, 3) THEN 1
    ELSE 0
  END AS flagged,
  0 AS active,
  'weekly-demo' AS meta,
  w.period_key,
  TIMESTAMP(w.week_start + INTERVAL 6 DAY, '18:00:00'),
  ctt.checklist_id,
  ctt.task_template_id
FROM checklist_task_templates ctt
JOIN template_tasks t ON t.id = ctt.task_template_id
JOIN demo_weeks w
WHERE ctt.checklist_id IN (@cl_food_weekly, @cl_alc_weekly);

-- =========================================================
-- 9. MONTHLY ACTIVATED TASKS (current month from demo range)
-- =========================================================

INSERT INTO activated_tasks
(completed, flagged, active, meta, period_key, ended_at, checklist_id, task_template_id)
SELECT
  CASE
    WHEN t.title IN ('Confirm compliance binder is updated')
         AND m.period_key = DATE_FORMAT(CURDATE(), '%Y-%m') THEN 0
    ELSE 1
  END AS completed,
  CASE
    WHEN t.title IN ('Confirm compliance binder is updated')
         AND m.period_key = DATE_FORMAT(CURDATE(), '%Y-%m') THEN 1
    ELSE 0
  END AS flagged,
  0 AS active,
  'monthly-demo' AS meta,
  m.period_key,
  TIMESTAMP(LAST_DAY(m.month_start), '17:00:00'),
  ctt.checklist_id,
  ctt.task_template_id
FROM checklist_task_templates ctt
JOIN template_tasks t ON t.id = ctt.task_template_id
JOIN demo_months m
WHERE ctt.checklist_id IN (@cl_food_monthly, @cl_alc_monthly);

-- =========================================================
-- 10. TEMPERATURE MEASUREMENTS
-- =========================================================

INSERT INTO temperature_measurements
(compliance_area, checklist_id, task_id, period_key, value_c, measured_at, organization_id, recorded_by_user_id)
SELECT
  'IK_MAT',
  atsk.checklist_id,
  atsk.id,
  atsk.period_key,
  CASE tt.title
    WHEN 'Check main fridge temperature' THEN
      CASE WHEN RIGHT(atsk.period_key, 2) = '08' THEN 6.20 ELSE 3.40 END
    WHEN 'Check prep fridge temperature' THEN
      CASE WHEN RIGHT(atsk.period_key, 2) IN ('03', '08') THEN 7.10 ELSE 3.80 END
    WHEN 'Check freezer room temperature' THEN
      CASE WHEN RIGHT(atsk.period_key, 2) = '05' THEN -16.50 ELSE -19.40 END
    WHEN 'Check cold storage temperature' THEN
      CASE WHEN RIGHT(atsk.period_key, 2) = '10' THEN 5.60 ELSE 2.70 END
    WHEN 'Check delivery temperature on receiving' THEN
      CASE WHEN RIGHT(atsk.period_key, 2) = '06' THEN 9.20 ELSE 5.10 END
    ELSE 3.50
  END,
  CASE
    WHEN atsk.meta = 'daily-demo' THEN TIMESTAMP(STR_TO_DATE(atsk.period_key, '%Y-%m-%d'), '10:00:00')
    WHEN atsk.meta = 'weekly-demo' THEN TIMESTAMP(CURDATE(), '11:00:00')
    ELSE TIMESTAMP(CURDATE(), '12:00:00')
  END,
  @org_id,
  CASE
    WHEN MOD(atsk.id, 2) = 0 THEN @kari_id
    ELSE @tore_id
  END
FROM activated_tasks atsk
JOIN template_tasks tt ON tt.id = atsk.task_template_id
WHERE tt.organisation_id = @org_id
  AND tt.section_type = 'TEMPERATURE_CONTROL';

-- =========================================================
-- 11. CHECK QUERIES
-- =========================================================

SELECT id, name, frequency, compliance_area, active_period_key
FROM checklists
WHERE organization_id = @org_id
ORDER BY compliance_area, frequency, name;

SELECT compliance_area, COUNT(*) AS template_count
FROM template_tasks
WHERE organisation_id = @org_id
GROUP BY compliance_area;

SELECT c.name AS checklist_name, c.frequency, COUNT(*) AS linked_tasks
FROM checklist_task_templates ctt
JOIN checklists c ON c.id = ctt.checklist_id
WHERE c.organization_id = @org_id
GROUP BY c.id, c.name, c.frequency
ORDER BY c.frequency, c.name;

SELECT c.name, c.frequency, COUNT(*) AS activated_count
FROM activated_tasks a
JOIN checklists c ON c.id = a.checklist_id
WHERE c.organization_id = @org_id
GROUP BY c.id, c.name, c.frequency
ORDER BY c.frequency, c.name;

SELECT
  c.name AS checklist_name,
  c.frequency,
  tt.title AS task_title,
  a.period_key,
  a.completed,
  a.flagged
FROM activated_tasks a
JOIN checklists c ON c.id = a.checklist_id
JOIN template_tasks tt ON tt.id = a.task_template_id
WHERE c.organization_id = @org_id
  AND (a.flagged = 1 OR a.completed = 0)
ORDER BY c.frequency, a.period_key DESC, c.name, tt.title;

SELECT
  c.name AS checklist_name,
  c.frequency,
  a.period_key,
  tt.title AS task_title,
  tm.value_c,
  tm.measured_at,
  HEX(tm.recorded_by_user_id) AS recorded_by
FROM temperature_measurements tm
JOIN activated_tasks a ON a.id = tm.task_id
JOIN checklists c ON c.id = a.checklist_id
JOIN template_tasks tt ON tt.id = a.task_template_id
WHERE tm.organization_id = @org_id
ORDER BY tm.measured_at DESC;

COMMIT;
