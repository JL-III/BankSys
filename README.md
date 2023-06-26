# BankSys

## A MySQL bank plugin  

Usage:  
  - `/bank deposit` to transfer money from a server to the bank.
  - `/bank withdraw` to pull money from the bank onto a server.
  - `/bank bal` to check the balance in the bank

The following limitations are set in the configuration file:
  - Minimum amount to initiate a transfer.
  - Cooldown per transfer