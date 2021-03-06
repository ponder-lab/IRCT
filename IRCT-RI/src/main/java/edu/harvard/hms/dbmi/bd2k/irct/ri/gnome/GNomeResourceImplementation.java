/**
 *
 */
package edu.harvard.hms.dbmi.bd2k.irct.ri.gnome;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.util.Utility;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GNomeResourceImplementation implements
		QueryResourceImplementationInterface, PathResourceImplementationInterface{

	Logger logger = Logger.getLogger(getClass());

	private String resourceName;
	private String resourceRootURL;
	private String gnomeUserName;
	private String gnomePassword;

	private String token;

	private ResourceState resourceState;

	private static final String AUTH_URL = "/auth/auth.cgi";

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * ResourceImplementationInterface#setup(java.util.Map)
	 */
	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException{
		if (logger.isDebugEnabled())
			logger.debug("setup for gNome"+
				" Starting...");

		String errorString = "";
		this.resourceName = parameters.get("resourceName");
		if (this.resourceName == null) {
			logger.error( "setup() `resourceName` parameter is missing.");
			errorString += " resourceName";
		}

		String tempResourceRootURL = parameters.get("resourceRootURL");
		if (tempResourceRootURL == null) {
			logger.error( "setup() `resourceRootURL` parameter is missing.");
			errorString += " resourceRootURL";
		} else {
			resourceRootURL = (tempResourceRootURL.endsWith("/"))?tempResourceRootURL.substring(0, tempResourceRootURL.length()-1):tempResourceRootURL;
		}

		this.gnomeUserName = parameters.get("gnomeUserName");
		if (this.gnomeUserName == null) {
			logger.error( "setup() `gnomeUserName` parameter is missing.");
			errorString += " gnomeUserName";
		}

		this.gnomePassword = parameters.get("gnomePassword");
		if (this.gnomePassword == null) {
			logger.error( "setup() `gnomePassword` parameter is missing.");
			errorString += " gnomePassword";
		}

		if (!errorString.isEmpty()) {
			throw new ResourceInterfaceException("GNome Interface setup() is missing:" + errorString);
		}

//		retrieveToken();

		resourceState = ResourceState.READY;
		logger.debug( "setup for " + resourceName +
				" Finished. " + resourceName +
						" is in READY state.");
	}

	@Override
	public String getType() {
		return null;
	}

	private boolean isTokenExists(){
		return token!=null && !token.isEmpty();
	}

	private void retrieveToken(){
		String urlString = resourceRootURL + AUTH_URL;

		CloseableHttpClient httpClient = IRCTApplication.CLOSEABLE_HTTP_CLIENT;
		HttpGet httpGet = new HttpGet(urlString);
		httpGet.addHeader("Authorization", "Basic " +
				DatatypeConverter.printBase64Binary((gnomeUserName+":"+gnomePassword)
						.getBytes()));
		CloseableHttpResponse response = null;

		try {
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();


			// the response body is in a Json format with a field "token"
			token = IRCTApplication.objectMapper.readTree(entity
					.getContent())
					.get("token")
					.textValue();

			EntityUtils.consume(entity);

			if (token != null && !token.isEmpty())
				logger.info("gNome token has been retrieved correctly");
			else
				logger.warn("gNome token has NOT been retrieved correctly with URL: " + urlString);

		} catch (IOException ex ){
			logger.error("IOException when retrieving token from gNome with url:" + urlString +
					" with exception message: " + ex.getMessage());
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException ex) {
				logger.error("GNOME - IOExcpetion when closing http response: " + ex.getMessage());
			}

		}
	}


	@Override
	public Result runQuery(User user, Query query, Result result) {
	    logger.debug("runQuery() starting.");

		retrieveToken();
		if (!isTokenExists()) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Cannot retrieve a token from gNome");
			return result;
		}

		List<WhereClause> whereClauses = query.getClausesOfType(WhereClause.class);

		result.setResultStatus(ResultStatus.CREATED);
		result.setMessage("Started running the query.");

		for (WhereClause whereClause : whereClauses) {

		    // Get the remote endpoint and put it in the result metadata field, as the endpointType
            String[] p = whereClause.getField().getPui().split("/");
            String endpointname = p[p.length-1];

			// http request
			String urlString = resourceRootURL + Utility.getURLFromPui(whereClause.getField().getPui(),resourceName);

			ObjectMapper objectMapper = IRCTApplication.objectMapper;
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put("token",token);

			Map<String, String> queries = whereClause.getStringValues();
			for (String key : queries.keySet()){
				objectNode.put(key, queries.get(key));
			}

			Map<String, Object> objectQueries = whereClause.getObjectValues();
			for (Map.Entry<String, Object> entry: objectQueries.entrySet()){
				objectNode.putPOJO(entry.getKey(), entry.getValue());
			}

			CloseableHttpClient client = IRCTApplication.CLOSEABLE_HTTP_CLIENT;
			HttpPost post = new HttpPost(urlString);
			try {
				post.setEntity(new StringEntity(objectMapper
						.writeValueAsString(objectNode), ContentType.APPLICATION_JSON));
			} catch (JsonProcessingException ex) {
				logger.error("runQuery() gNome - Error when generating Json post body: " + ex.getMessage());
			}

			CloseableHttpResponse response = null;

			// http response
			try {
				result.setResultStatus(ResultStatus.RUNNING);

				response = client.execute(post);

				// Add error handling if the remote system does not respond with a 200 status code.
				if (response.getStatusLine().getStatusCode()!=200) {
				    logger.error("runQuery() the remote system responded with a non 200 status code, the response content is: " +
							EntityUtils.toString(response.getEntity()));
				    result.setResultStatus(ResultStatus.ERROR);
				    result.setMessage(response.getStatusLine().getReasonPhrase());
				    return result;
                }
				HttpEntity entity = response.getEntity();

				// PICSURE-79 Special handing for subset endpoint. It does NOT create a `matrix` field in the
                // response, so we have to parse it differently
                if (endpointname.equalsIgnoreCase("subset_api.cgi")) {
                    // In the old days, GNOME would return a `status` field and a `matrix` field. and it would get returned to
                    // the requestor,
                    // in case there was no parsing of the response. As a fallback. This is NOT true for some endpoints, which
                    // require some special handling, and ignoring whether the "status" field returns anything
                    FileResultSet frs = (FileResultSet) result.getData();
                    handleEndpointResponse(endpointname,objectMapper
                            .readTree(entity
                                    .getContent()),frs);
                    result.setData(frs);

                } else {
                    // parsing data
                    parseData(result, objectMapper
                            .readTree(entity
                                    .getContent()));
                }

				result.setResultStatus(ResultStatus.COMPLETE);
				result.setMessage("Finished parsing data from gNome");

				EntityUtils.consume(entity);

			} catch (PersistableException ex) {
				result.setResultStatus(ResultStatus.ERROR);
				logger.error("Persistable error: " + ex.getMessage() );
			} catch (ResultSetException ex) {
				result.setResultStatus(ResultStatus.ERROR);
				logger.error("Cannot append row: " + ex.getMessage());
			} catch (JsonParseException ex){
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("Cannot parse gnome response as a JsonNode");
				logger.error("Cannot parse response as a JsonNode: " + ex.getMessage());
			} catch (IOException ex ){
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("Cannot execute Post request to gnome");
				logger.error("IOException: Cannot cannot execute POST with URL: " + urlString);
			} finally {
				try {
					if (response != null)
						response.close();
				} catch (IOException ex) {
					logger.error("IOException when closing http response instance: " + ex.getMessage());
				}
			}


		}
		return result;
	}

	private void handleEndpointResponse(String endpointName, JsonNode endpointResponse, FileResultSet frs) {
	    logger.debug("handleEndpointResponse() starting...");

        logger.debug("handleEndpointResponse() processing:"+endpointName);
        switch (endpointName) {
            case "subset_api.cgi":
                try {
                    frs.appendColumn(new Column("subset_type", PrimitiveDataType.STRING));
                    frs.appendColumn(new Column("subset_name", PrimitiveDataType.STRING));
                    frs.appendRow();
                    frs.updateString("subset_type", "A");
                    frs.updateString("subset_name", endpointResponse.get("project_type_A").asText());
                    frs.appendRow();
                    frs.updateString("subset_type", "B");
                    frs.updateString("subset_name", endpointResponse.get("project_type_B").asText());
                } catch (ResultSetException e) {
                    e.printStackTrace();
                } catch (PersistableException e) {
                    e.printStackTrace();
                }
                break;
            default:
                try {
                    frs.appendColumn(new Column("status", PrimitiveDataType.STRING));
                    frs.appendColumn(new Column("message", PrimitiveDataType.STRING));
                    frs.appendRow();
                    frs.updateString("status", "error");
                    frs.updateString("message", "Unknown endpoint '"+endpointName+"', the response from remote datasource is not handled.");
                } catch (ResultSetException e) {
                    e.printStackTrace();
                } catch (PersistableException e) {
                    e.printStackTrace();
                }
        }
    }

	private void parseData(Result result, JsonNode responseJsonNode)
			throws PersistableException, ResultSetException{
		logger.debug("parseData() starting...");

		FileResultSet frs = (FileResultSet) result.getData();

        logger.debug("parseData() parsing response with `status` field in it.");
        // This is the original handling, when a so called `matrix` datastructure is returned.
        String responseStatus = responseJsonNode.get("status").textValue();

        JsonNode matrixNode = responseJsonNode.get("matrix");
        logger.debug("parseData() parsing `matrix` object of the response.");
            if (responseStatus.equalsIgnoreCase("success")){
                if (!matrixNode.getNodeType().equals(JsonNodeType.ARRAY)
                        || !matrixNode.get(0).getNodeType().equals(JsonNodeType.ARRAY)){
                    String errorMessage = "Cannot parse response JSON from gnome: expecting an 2D array";
                    result.setMessage(errorMessage);
                    throw new PersistableException(errorMessage);
                }

                // append columns
                for (JsonNode innerJsonNode : matrixNode.get(0)){
                    if (!innerJsonNode.getNodeType().equals(JsonNodeType.STRING)){
                        String errorMessage = "Cannot parse response JSON from gnome: expecting a String in header array";
                        result.setMessage(errorMessage);
                        throw new PersistableException(errorMessage);
                    }

                    // how can I know what datatype it is for now?... just set it primitive string...
                    frs.appendColumn(new Column(innerJsonNode.textValue(), PrimitiveDataType.STRING));
                }

                // append rows
                for (int i = 1; i < matrixNode.size(); i++){
                    JsonNode jsonNode = matrixNode.get(i);
                    if (!jsonNode.getNodeType().equals(JsonNodeType.ARRAY)){
                        String errorMessage = "Cannot parse response JSON from gnome: expecting an 2D array";
                        result.setMessage(errorMessage);
                        throw new PersistableException(errorMessage);
                    }

                    frs.appendRow();

                    for (int j = 0; j<jsonNode.size(); j++){
                        // column datatype could be reset here by checking the json NodeType,
                        // but no PrimitiveDataType.NUMBER implemented yet, can't efficiently separate
                        // integer, double, just store everything as STRING for now
                        frs.updateString(frs.getColumn(j).getName(),
                                jsonNode.get(j).asText());
                    }

                }
            } else {
                frs.appendColumn(new Column("status", PrimitiveDataType.STRING));
                frs.appendColumn(new Column("message", PrimitiveDataType.STRING));
                frs.appendRow();
                frs.updateString("status", responseStatus);
                frs.updateString("message", responseJsonNode.get("message").textValue());
            }

		result.setData(frs);
	}

	@Override
	public Result getResults(User user, Result result) {
		logger.debug( "getResults() Starting ...");

		return result;
	}

	@Override
	public ResourceState getState() {
		return resourceState;
	}

	@Override
	public ResultDataType getQueryDataType(Query query) {
		return ResultDataType.TABULAR;
	}

	@Override
	public List<Entity> getPathRelationship(Entity path, OntologyRelationship relationship, User user) throws ResourceInterfaceException {

		if (!path.getPui().equals(resourceName)
                && !path.getPui().equals("/"+resourceName)
				&& !path.getPui().equals("/" + resourceName + "/")){
			return null;
		}

		List<Entity> entities = new ArrayList<>();
		entities.add(
				new Entity("/" + resourceName +"/analyze_genes_rest.cgi", "For genes analyze"));
		entities.add(
				new Entity("/" + resourceName + "/analyze_variants_rest.cgi", "For variants analyze"));
		entities.add(
				new Entity("/" + resourceName + "/query_rest.cgi", "Query rest api for both genes and variants"));
		entities.add(
				new Entity( "/" + resourceName + "/subset_api.cgi", "Return subset ID for a given list of patient IDs"));

		return entities;
	}

	@Override
	public List<Entity> find(Entity path, FindInformationInterface findInformation, User user) {
		throw new NotImplementedException();
	}
}
