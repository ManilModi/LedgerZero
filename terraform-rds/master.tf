resource "aws_db_instance" "master" {
  identifier            = "ledger-master-db"

  engine                = "postgres"
  engine_version        = "17.6"
  instance_class        = "db.t4g.micro"

  allocated_storage     = 20
  storage_type          = "gp3"
  max_allocated_storage = 100

  db_name               = "bank_db"

  publicly_accessible   = true
  multi_az              = false

  storage_encrypted     = true
  kms_key_id            = "arn:aws:kms:ap-south-1:034615892571:key/05ae0116-d05e-423e-870c-0308e861df5c"

  backup_retention_period = 1
  copy_tags_to_snapshot  = true

  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  skip_final_snapshot   = true

  vpc_security_group_ids = [
    "sg-019bc0f6d9803b498"
  ]
}
