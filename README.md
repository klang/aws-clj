# AWS EC2 experiments

The foundation for these experiments start with [mrowe/clj-aws-ec2](https://github.com/mrowe/clj-aws-ec2) which is an old library for accessing Amazon EC2.

I don't know if the AWS interested part of the community have moved on to something else over the last years, but I found this library usefull for my small experiments.

## ec2-client

I have access to several aws accounts and I want to hook into the standard [configuration](https://gist.github.com/klang/877fabe24f14ef26f0bfe7891de6dd09) that can be used with aws-cli and boto (Python) too.

The ability to simply state a profile (or let the DefaultAWSCredentialsProviderChain resolve to an AMI role attached to the EC2 instance running the overall program) is quite strong.

## waiters

It's really difficult to make any kind of operation against the AWS system, without having the ability to wait for an operation to finish. Operations are not always executed instantly, it takes time to start an Instance and it takes time for the network card of a terminated Instance to release the public ip address.

Some waiters are described directly in the latest version of the AWS Java API, some are not.

This snippet includes example uses for some of the waiters.

## launch-another-like-this

A standard functionality that is part of the AWS Console .. nothing special, but it shows some usage of the library as a whole.

## reset-instance

A slightly more advanced example.

For test purposes, I have an AMI (Amazon Machine Image) containing a baseline installation of a system, including a database with baseline data. I needed a way to "get back to the initial state" of this machine, without changing the public and private ip adresses (Oracle doesn't like it, when you change the ip-adressses).

This snippet let's me easily scrap an instance after the database has gone out of wack and get back to a known state.

The snippet will also let me change the instance-type or the image-id during a reset.

This shows a usage of waiters.

## tools

Stuff that is used across the different examples. Heavy use of "extend-protocol" is used to give the definitions from aws.sdk.ec2 a slightly better naming convension (my opinion) and additional data in some of the special cases that I needed.

There MAY be more "extend-protocol" definitions than visibly needed, but I have other experiments that are not ready for public consumption yet :-)



