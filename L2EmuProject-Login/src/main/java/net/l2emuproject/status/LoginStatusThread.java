/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.l2emuproject.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import net.l2emuproject.loginserver.beans.Accounts;
import net.l2emuproject.loginserver.manager.LoginManager;
import net.l2emuproject.status.StatusThread;
import net.l2emuproject.status.commands.Restart;
import net.l2emuproject.status.commands.Shutdown;
import net.l2emuproject.status.commands.Statistics;
import net.l2emuproject.status.commands.UnblockIP;
import net.l2emuproject.tools.codec.Base64;


public final class LoginStatusThread extends StatusThread
{
	public LoginStatusThread(LoginStatusServer server, Socket socket) throws IOException
	{
		super(server, socket);
		
		register(new Shutdown());
		register(new Restart());
		register(new Statistics());
		register(new UnblockIP());
	}
	
	@Override
	protected boolean login() throws IOException
	{
		print("Account: ");
		final String account = readLine();
		print("Password: ");
		final String password = readLine();
		
		if (!isValidGMAccount(account, password))
		{
			println("Incorrect login!");
			return false;
		}
		
		return true;
	}
	
	private boolean isValidGMAccount(String account, String password)
	{
		final Accounts acc = LoginManager.getInstance().getAccount(account);
		
		if (!LoginManager.getInstance().isGM(acc))
			return false;
		
		final String expectedPassword = acc.getPassword();
		
		if (expectedPassword == null || password == null)
			return false;
		
		try
		{
			final byte[] expectedPass = Base64.decode(expectedPassword);
			final byte[] givenPass = MessageDigest.getInstance("SHA").digest(password.getBytes("UTF-8"));
			
			return Arrays.equals(expectedPass, givenPass);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}
