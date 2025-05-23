Settle
---

Application for personal group balance keeping.

Installation
---

Clone the repo: git clone ``https://github.com/DevSteffensen/Settle.git``\
Go to the project directory: cd Settle
Build app: ./mvn clean package

Running the app:
---
``java -jar target/Settle-1.0-SNAPSHOT.jar``\
this command runs the app, now command line works as console.\
all commands are described in ``help`` command.


## User Management

- **`add user`**  
  Adds a user to the database.  
  *Note:* Only users known to the system can owe or be owed a debt.

- **`get users`**  
  Lists all users currently registered in the system.


## Expense Management

- **`add simple expense`**  
  Adds a simple expense to the system.  
  The system will prompt you to:  
  1. Select a payer  
  2. Enter the value  
  3. Specify a payee

- **`add expense`**  
  Adds a complex expense, which can be split among multiple debtors or have different values owed by each.



## Reporting

- **`get balance`**  
  Retrieves the current balance information, showing who owes what debt.

- **`total traffic`**  
  Displays the total amount of CZK tracked in the system.

- **`total debt`**  
  Shows how much a user has spent through the system.

- **`average transaction`**  
  Displays the average debt amount per transaction.

- **`average debt`**  
  Shows the average debt amount per user.



## Administration

- **`drop tables`**  
  Deletes all information from the databases.  
  *Warning:* This action is irreversible and will erase all data.



## Exit

- **`exit`**  
  Exits the program.

