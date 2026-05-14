variable "aws_region" {
  description = "AWS region used by MiniStack resources"
  type        = string
  default     = "us-east-1"
}

variable "aws_endpoint_url" {
  description = "MiniStack endpoint URL"
  type        = string
  default     = "http://localhost:4566"
}

variable "aws_access_key_id" {
  description = "Dummy key for MiniStack"
  type        = string
  default     = "test"
}

variable "aws_secret_access_key" {
  description = "Dummy secret for MiniStack"
  type        = string
  default     = "test"
}
