package com.giyeok.oneswt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SwtLoaderTest {
    public static void main(String[] args) {
        new SwtLoader().loadSwt();

        Display display = new Display();
        Shell shell = new Shell(display);

        shell.setText("SWT Loader Test");
        shell.setBounds(50, 50, 500, 400);

        shell.setLayout(new FillLayout());

        Label label = new Label(shell, SWT.NONE);
        label.setText("It works!");

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
