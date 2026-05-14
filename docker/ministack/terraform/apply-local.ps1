$ErrorActionPreference = "Stop"

$terraformDir = Split-Path -Parent $MyInvocation.MyCommand.Path

terraform -chdir="$terraformDir" init

terraform -chdir="$terraformDir" workspace select -or-create local

terraform -chdir="$terraformDir" apply -auto-approve


