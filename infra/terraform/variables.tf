variable "aws_region" { description = "AWS deployment region"; type = string; default = "ap-south-1" }
variable "environment" { description = "Environment name"; type = string; default = "dev"; validation { condition = contains(["dev", "staging", "prod"], var.environment); error_message = "environment must be dev, staging, or prod." } }
