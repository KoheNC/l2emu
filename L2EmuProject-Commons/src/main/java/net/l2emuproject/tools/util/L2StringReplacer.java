package net.l2emuproject.tools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author lord_rex, thanks NB4L1 for base.
 */
public class L2StringReplacer
{
	private static final String				OLDTEXT		= "old text";

	private static final String				NEWTEXT		= "new text";

	private static final ArrayList<String>	MODIFIED	= new ArrayList<String>();

	public static void main(String[] args) throws IOException
	{
		parse(new File("."));

		System.out.println();

		if (MODIFIED.isEmpty())
		{
			System.out.println("There was no modification.");
		}
		else
		{
			System.out.println("Modified files:");
			System.out.println("================");

			for (String line : MODIFIED)
				System.out.println(line);
		}

		System.out.flush();
	}

	private static final FileFilter	FILTER	= new FileFilter()
											{
												@Override
												public boolean accept(File f)
												{
													// to skip svn files
													if (f.isHidden())
														return false;

													if (f.getName().equals("L2StringReplacer.java"))
														return false;

													return f.isDirectory() || f.getName().endsWith("java");
												}
											};

	private static void parse(File f) throws IOException
	{
		System.out.println(f.getCanonicalPath());

		if (f.isDirectory())
			for (File tmp : f.listFiles(FILTER))
				parse(tmp);
		else
		{
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = "", oldtext = "";
			while ((line = reader.readLine()) != null)
			{
				oldtext += line + "\r\n";
			}
			reader.close();

			// To replace a line in a file
			String newtext = oldtext.replace(OLDTEXT, NEWTEXT);

			FileWriter writer = new FileWriter(f);
			writer.write(newtext);
			writer.close();

			MODIFIED.add(f.getPath());
		}
	}
}
