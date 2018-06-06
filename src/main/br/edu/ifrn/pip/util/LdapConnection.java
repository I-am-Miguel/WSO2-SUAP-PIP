package br.edu.ifrn.pip.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.edu.ifrn.pip.SuapAttributeFinder;
import br.edu.ifrn.pip.factory.Factory;

public class LdapConnection {
	private static Log log = LogFactory.getLog(SuapAttributeFinder.class);

    //TODO: implementar singleton para conexão
    

    public static String buscaUsuarioDepartamento(String stringBusca) {
    		log.info("Conectando ao ldap...");
		String ldapUsuario = "";
		String ldapSenha = "";
		String ldapServidor = "";
		String departamento = "";

		//TODO: criar classe utilitária para recuperar configuração (usar singleton)
    		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("wso2-pip-suap.properties");		
    		if (inputStream != null) {
    			Properties properties = new Properties();
    			try {
    				properties.load(inputStream);
    				
    				ldapUsuario = properties.getProperty("ldap.usuario");
    				ldapSenha = properties.getProperty("ldap.senha");
    				ldapServidor = properties.getProperty("ldap.servidor");

    			} catch (IOException exception) {
    				exception.printStackTrace();
    			}
    		}
    		
		Hashtable<String, String> authEnv = new Hashtable<String, String>(11);
		authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		authEnv.put(Context.PROVIDER_URL, ldapServidor);
		authEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		authEnv.put(Context.SECURITY_PRINCIPAL, ldapUsuario);
		authEnv.put(Context.SECURITY_CREDENTIALS, ldapSenha);

		try {
			DirContext authContext = new InitialDirContext(authEnv);
			log.info("Autenticação LDAP OK! Buscando por usuário " + stringBusca + "...");

			try {

				SearchControls constraints = new SearchControls();
				constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

				String[] attrIDs = { "distinguishedName", "sn", "givenname", "mail", "department" };
				constraints.setReturningAttributes(attrIDs);

				NamingEnumeration<?> answer = authContext.search("OU=IFRN,DC=ifrn,DC=local", "sAMAccountName=" + stringBusca, constraints);
				if (answer.hasMore()) {
					Attributes attrs = ((SearchResult) answer.next()).getAttributes();
					departamento = attrs.get("department").get().toString();
					log.info("Usuário encontrado (departamento: " + departamento + ")");
					
				} else {
					log.error("Usuário Inválido");
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		} catch (AuthenticationException authEx) {
			log.error("Erro na autenticação! ");
			authEx.printStackTrace();

		} catch (NamingException namEx) {
			log.error("Problemas na conexão! " + namEx.toString());
		}
		
		return departamento;
	}
}
