## Market Mood

Tool to examine market mood

/api/company

 * GET - retrieval
  * retrieve all companies in database
 * PUT - creation
  * content has json - create from json
 
/api/company/:id

 * GET - retrieval using identifier
 * POST - update company information
  * content has json to update company
 * DELETE - delete using identifier

/api/company/:id/analysis
 * GET - retrieve current analysis information
  * PUT - start analyzing information about a company
  * content has json to describe how to analyze (see analysis)
 * PUT - update analysis settings
  * content has json to describe how to analyze (see analysis)
 * DELETE - stop analysis for company
 
/api/company/:id/notification/:email
 * GET - retrieve email settings email in notification list
 * PUT - add an e-mail to notification list for company
  * content has json to describe notification settings (see notification)
 * POST - update notification settings
  * content has json to describe notification settings (see notification)
 * DELETE - remove email for notification list

## Company information
Describes parameters for updating company information

## Analysis
Describes parameters for starting a stock analysis

## Notification
Describes parameters for receiving notification about company