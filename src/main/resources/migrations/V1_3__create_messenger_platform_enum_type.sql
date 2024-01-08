DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'messenger_platform_enum_type') THEN
        create type messenger_platform_enum_type AS ENUM ('TELEGRAM');
    END IF;
END
$$;