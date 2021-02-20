# TODO NOTE not yet tested...just manually add in GUI
variable "aws_instance_type" {
  type        = string
  description = "instance_type for aws ec2 instance"
}

variable "aws_region" {
  type        = string
  description = "region for aws ec2 instance"
}

provider "aws" {
  version                 = "3.0.0"
  profile                 = "i-graph-dse-ec2"
  shared_credentials_file = "/workspace/.aws/credentials"
  region                  = var.aws_region
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloud9_environment_ec2
# TODO set name dynamically?
resource "aws_instance" "i-graph-dse" {
  instance_type = var.aws_instance_type
  name          = "i-graph-dse-${var.aws_instance_type}-from-terraform"
  # TODO actually currently is aws linux, and cannot change I don't think
  description = "ec2 box for dse using aws linux with 16GB Ram and 4 cores"
  # ami           = data.aws_ami.ubuntu.id
  ami           = ami-07a29e5e945228fa1
  # instance_type = "t3.micro"
  instance_type = "r6g.medium"
  tags = {
    Terraform = "true"
    intertextualityGraph = "true"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/eip
resource "aws_eip" "lb" {
  instance = aws_instance.i-graph-dse.id
  vpc      = true
}

output "id" {
  description = "List of IDs of instances"
  value       = aws_ec2.i-graph-dse.*.id
}

