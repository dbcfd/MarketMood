## Markit API Test

Test of the Markit API.

Current supported methods:

/api/company - retrieves all companies currently in clone database
 * GET - retreival
/api/company/:symbol - retrieves information about a company with symbol
 * GET - retrieval, if no company in clone, will look at markit database
 * PUT - creation
  * content has json - create from json
  * content is empty - create from markit lookup
 * DELETE - delete from clone database
/api/company/name=:name - retrieves information about all companies with name information matching
 * GET - retrieval, if no companies in clone, look at markit
/api/company/:symbol/SeriesData?start=:start+end=:end
 * GET - retrieve time series information (not implemented)