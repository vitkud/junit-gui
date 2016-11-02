package ru.vitkud.test;

import java.beans.Beans;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

public class TestRunner implements ITestListener {

	/**
	 * Launch the application.
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("Missing Test class name");
			System.exit(2);
		}
		System.exit(runTest(Class.forName(args[0])));
	}

	public static int runTest(Class<?> test) {
		TestRunner window = new TestRunner();
		window.setSuite(test);
		window.open();
		return window.getFailureCount();
	}

	private static final String TEST_INI_FILE = "junit-gui.properties";
	private static final String CN_CONFIG_INI_SECTION = "Config";

	protected Display display;
	protected Shell shell;
	private ToolBar toolBar;
	private Tree testTree;
	private Table tableResults;
	private Table tableFailureList;
	private MenuItem mntmSelectAll;
	private MenuItem mntmDeselectAll;
	private MenuItem mntmSelectFailed;
	private MenuItem mntmSelectCurrent;
	private MenuItem mntmDeselectCurrent;
	private MenuItem mntmHideTestNodes;
	private MenuItem mntmExpandAll;
	private MenuItem mntmCopyTestnameToClipboard;
	private MenuItem mntmRunSelectedTest;
	private MenuItem mntmAutoSave;
	private MenuItem mntmErrorBoxVisible;
	private MenuItem mntmHideTestNodesOnOpen;
	private MenuItem mntmShowTestedNode;
	private MenuItem mntmBreakOnFailures;
	private MenuItem mntmUseRegistry;
	private MenuItem mntmWarnIfFailTestOverridden;
	private MenuItem mntmFailTestCaseIfNoChecksExecuted;
	private MenuItem mntmReportMemoryLeakTypeOnShutdown;
	private MenuItem mntmFailTestCaseIfMemoryLeaked;
	private MenuItem mntmIgnoreMemoryLeakInSetUpTearDown;
	private MenuItem mntmRun;
	private MenuItem mntmRunSelectedTest2;
	private MenuItem mntmStop;
	private MenuItem mntmCopyErrorMessageToClipboard;
	private ToolItem tltmSelectAll;
	private ToolItem tltmDeselectAll;
	private ToolItem tltmSelectFailed;
	private ToolItem tltmSelectCurrent;
	private ToolItem tltmDeselectCurrent;
	private ToolItem tltmRun;
	private ToolItem tltmRunselectedtest;
	private ToolItem tltmStop;
	private SashForm sashForm;
	private Composite compositeResults;
	private ProgressBar progressBar;
	private ProgressBar scoreBar;
	private Label lblProgressPercent;
	private Composite compositeErrorBox;
	private StyledText errorMessageStyledText;
	private MenuItem pmntmSelectCurrent;
	private MenuItem pmntmDeselectCurrent;
	private MenuItem pmntmSelectFailed;
	private MenuItem pmntmSelectAll;
	private MenuItem pmntmDeselectAll;
	private MenuItem pmntmHideTestNodes;
	private MenuItem pmntmExpandAll;
	private MenuItem pmntmCopyTestnameToClipboard;
	private MenuItem pmntmRunSelectedTest;
	private MenuItem pmntmCopyErrorMessageToClipboard;

	// Color constants for the progress bar and failure details panel
	private Color clOk;// = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	private Color clFailure;// = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
	private Color clError;// = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	// color images used in the test tree and failure list
	private Image imgNone;
	private Image imgRunning;
	private Image imgRun;
	private Image imgHasProps;
	private Image imgFailed;
	private Image imgError;
	private Map<Image, Integer>	indexesImages;

	public TestRunner() {
		display = Display.getDefault();
		createContents();

		if (!Beans.isDesignTime()) {
			formCreate();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception e) {
				if (shell.isDisposed())
					throw e;
				showError(null, e);
			}
		}
	}

	// XXX @Deprecated
	public void processMessages() {
		if (shell.isDisposed())
			throw new SWTException("Application is closed");
		else
			while (display.readAndDispatch());
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				formShow();
			}
		});
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				formDestroy();
			}
		});
		shell.setMinimumSize(new Point(300, 200));
		shell.setSize(500, 500);
		shell.setText("JUnit GUI");
		FormLayout fl_shell = new FormLayout();
		fl_shell.marginBottom = 1;
		fl_shell.marginRight = 4;
		fl_shell.marginLeft = 4;
		shell.setLayout(fl_shell);

		Menu mainMenu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mainMenu);

		MenuItem mntmFile = new MenuItem(mainMenu, SWT.CASCADE);
		mntmFile.setText("&File");

		Menu fileMenu = new Menu(mntmFile);
		mntmFile.setMenu(fileMenu);

		MenuItem mntmSaveConfiguration = new MenuItem(fileMenu, SWT.NONE);
		mntmSaveConfiguration.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveConfiguration();
			}
		});
		mntmSaveConfiguration.setAccelerator(SWT.CTRL | 'S');
		mntmSaveConfiguration.setText("&Save Configuration" + "\tCtrl+S");

		MenuItem mntmRestoreSavedConfiguration = new MenuItem(fileMenu, SWT.NONE);
		mntmRestoreSavedConfiguration.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadConfiguration();
				runActionUpdate();
			}
		});
		mntmRestoreSavedConfiguration.setText("&Restore Saved Configuration");

		MenuItem mntmExit = new MenuItem(fileMenu, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fTestResult != null && fNotifier != null)
					fNotifier.pleaseStop();
				shell.close();
			}
		});
		mntmExit.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/6.png"));
		mntmExit.setAccelerator(SWT.ALT | 'X');
		mntmExit.setText("E&xit" + "\tAlt+X");

		MenuItem mntmTestTree = new MenuItem(mainMenu, SWT.CASCADE);
		mntmTestTree.setText("Test &Tree");

		Menu testTreeMenu = new Menu(mntmTestTree);
		mntmTestTree.setMenu(testTreeMenu);

		mntmSelectAll = new MenuItem(testTreeMenu, SWT.NONE);
		mntmSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAllActionExecute();
			}
		});
		mntmSelectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/0.png"));
		mntmSelectAll.setAccelerator(SWT.CTRL | SWT.ALT | 'A');
		mntmSelectAll.setText("Select &All" + "\tCtrl+Alt+A");

		mntmDeselectAll = new MenuItem(testTreeMenu, SWT.NONE);
		mntmDeselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deselectAllActionExecute();
			}
		});
		mntmDeselectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/1.png"));
		mntmDeselectAll.setAccelerator(SWT.CTRL | SWT.DEL);
		mntmDeselectAll.setText("&Deselect All" + "\tCtrl+Del");

		mntmSelectFailed = new MenuItem(testTreeMenu, SWT.NONE);
		mntmSelectFailed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectFailedActionExecute();
			}
		});
		mntmSelectFailed.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/4.png"));
		mntmSelectFailed.setAccelerator(SWT.CTRL | 'F');
		mntmSelectFailed.setText("Select Fai&led" + "\tCtrl+F");

		mntmSelectCurrent = new MenuItem(testTreeMenu, SWT.NONE);
		mntmSelectCurrent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCurrentActionExecute();
			}
		});
		mntmSelectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/2.png"));
		mntmSelectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | 'A');
		mntmSelectCurrent.setText("Select &Current" + "\tShift+Ctrl+A");

		mntmDeselectCurrent = new MenuItem(testTreeMenu, SWT.NONE);
		mntmDeselectCurrent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deselectCurrentActionExecute();
			}
		});
		mntmDeselectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/3.png"));
		mntmDeselectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | SWT.DEL);
		mntmDeselectCurrent.setText("Deselect C&urrent" + "\tShift+Ctrl+Del");

		new MenuItem(testTreeMenu, SWT.SEPARATOR);

		mntmHideTestNodes = new MenuItem(testTreeMenu, SWT.NONE);
		mntmHideTestNodes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hideTestNodesActionExecute();
			}
		});
		mntmHideTestNodes.setAccelerator(SWT.CTRL | 'H');
		mntmHideTestNodes.setText("&Hide Test Nodes" + "\tCtrl+H");

		mntmExpandAll = new MenuItem(testTreeMenu, SWT.NONE);
		mntmExpandAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				expandAllNodesActionExecute();
			}
		});
		mntmExpandAll.setAccelerator(SWT.CTRL | 'P');
		mntmExpandAll.setText("Ex&pand All" + "\tCtrl+P");

		new MenuItem(testTreeMenu, SWT.SEPARATOR);

		MenuItem mntmGoToNextSelectedNode = new MenuItem(testTreeMenu, SWT.NONE);
		mntmGoToNextSelectedNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goToNextSelectedTestActionExecute();
			}
		});
		mntmGoToNextSelectedNode.setAccelerator(SWT.CTRL | SWT.ARROW_RIGHT);
		mntmGoToNextSelectedNode.setText("Go To Next Selected Test" + "\tCtrl+Right");

		MenuItem mntmGoToPreviousSelectedNode = new MenuItem(testTreeMenu, SWT.NONE);
		mntmGoToPreviousSelectedNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goToPrevSelectedTestActionExecute();
			}
		});
		mntmGoToPreviousSelectedNode.setAccelerator(SWT.CTRL | SWT.ARROW_LEFT);
		mntmGoToPreviousSelectedNode.setText("Go To Previous Selected Test" + "\tCtrl+Left");

		new MenuItem(testTreeMenu, SWT.SEPARATOR);

		mntmCopyTestnameToClipboard = new MenuItem(testTreeMenu, SWT.NONE);
		mntmCopyTestnameToClipboard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyTestnameToClipboardActionExecute();
			}
		});
		mntmCopyTestnameToClipboard.setAccelerator(SWT.CTRL | SWT.ALT | 'C');
		mntmCopyTestnameToClipboard.setText("Copy testname to clipboard" + "\tCtrl+Alt+C");

		new MenuItem(testTreeMenu, SWT.SEPARATOR);

		mntmRunSelectedTest = new MenuItem(testTreeMenu, SWT.NONE);
		mntmRunSelectedTest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runSelectedTestActionExecute();
			}
		});
		mntmRunSelectedTest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		mntmRunSelectedTest.setAccelerator(SWT.F8);
		mntmRunSelectedTest.setText("Run selected test" + "\tF8");

		MenuItem mntmOptions = new MenuItem(mainMenu, SWT.CASCADE);
		mntmOptions.setText("&Options");

		Menu optionsMenu = new Menu(mntmOptions);
		mntmOptions.setMenu(optionsMenu);

		mntmAutoSave = new MenuItem(optionsMenu, SWT.CHECK);
		mntmAutoSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				autoSaveConfiguration();
			}
		});
		mntmAutoSave.setSelection(true);
		mntmAutoSave.setText("&Auto Save Configuration");

		mntmErrorBoxVisible = new MenuItem(optionsMenu, SWT.CHECK);
		mntmErrorBoxVisible.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				compositeErrorBox.setVisible(mntmErrorBoxVisible.getSelection());
				shell.layout(true, true);
			}
		});
		mntmErrorBoxVisible.setSelection(true);
		mntmErrorBoxVisible.setText("&Error Box Visible");

		MenuItem mntmAutoChangeFocus = new MenuItem(optionsMenu, SWT.CHECK);
		mntmAutoChangeFocus.setSelection(true);
		mntmAutoChangeFocus.setText("Auto Change &Focus");

		mntmHideTestNodesOnOpen = new MenuItem(optionsMenu, SWT.CHECK);
		mntmHideTestNodesOnOpen.setText("&Hide Test Nodes On Open");

		mntmShowTestedNode = new MenuItem(optionsMenu, SWT.CHECK);
		mntmShowTestedNode.setSelection(true);
		mntmShowTestedNode.setText("&Show Tested Node");

		mntmBreakOnFailures = new MenuItem(optionsMenu, SWT.CHECK);
		mntmBreakOnFailures.setText("&Break on Failures");

		mntmUseRegistry = new MenuItem(optionsMenu, SWT.CHECK);
		mntmUseRegistry.setText("Use Registry");

		new MenuItem(optionsMenu, SWT.SEPARATOR);

		MenuItem mntmShowTestCasesWithRunTimeProperties = new MenuItem(optionsMenu, SWT.CHECK);
		mntmShowTestCasesWithRunTimeProperties.setText("Show TestCases with RunTime Properties");

		mntmWarnIfFailTestOverridden = new MenuItem(optionsMenu, SWT.CHECK);
		mntmWarnIfFailTestOverridden.setText("Warn if Fail Test Overridden");

		new MenuItem(optionsMenu, SWT.SEPARATOR);

		mntmFailTestCaseIfNoChecksExecuted = new MenuItem(optionsMenu, SWT.CHECK);
		mntmFailTestCaseIfNoChecksExecuted.setText("Fail TestCase if no checks executed");

		new MenuItem(optionsMenu, SWT.SEPARATOR);

		mntmReportMemoryLeakTypeOnShutdown = new MenuItem(optionsMenu, SWT.CHECK);
		mntmReportMemoryLeakTypeOnShutdown.setText("Report memory leak type on Shutdown");

		mntmFailTestCaseIfMemoryLeaked = new MenuItem(optionsMenu, SWT.CHECK);
		mntmFailTestCaseIfMemoryLeaked.setText("Fail TestCase if memory leaked");

		mntmIgnoreMemoryLeakInSetUpTearDown = new MenuItem(optionsMenu, SWT.CHECK);
		mntmIgnoreMemoryLeakInSetUpTearDown.setEnabled(false);
		mntmIgnoreMemoryLeakInSetUpTearDown.setText("Ignore memory leak in SetUp/TearDown");

		MenuItem mntmActions = new MenuItem(mainMenu, SWT.CASCADE);
		mntmActions.setText("Actio&ns");

		Menu actionsMenu = new Menu(mntmActions);
		mntmActions.setMenu(actionsMenu);

		mntmRun = new MenuItem(actionsMenu, SWT.NONE);
		mntmRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runActionExecute();
			}
		});
		mntmRun.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/7.png"));
		mntmRun.setAccelerator(SWT.F9);
		mntmRun.setText("Run" + "\tF9");

		mntmRunSelectedTest2 = new MenuItem(actionsMenu, SWT.NONE);
		mntmRunSelectedTest2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runSelectedTestActionExecute();
			}
		});
		mntmRunSelectedTest2.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		mntmRunSelectedTest2.setAccelerator(SWT.F8);
		mntmRunSelectedTest2.setText("Run selected test" + "\tF8");

		mntmStop = new MenuItem(actionsMenu, SWT.NONE);
		mntmStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stopActionExecute();
			}
		});
		mntmStop.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/8.png"));
		mntmStop.setAccelerator(SWT.CTRL | SWT.F2);
		mntmStop.setText("&Stop" + "\tCtrl+F2");

		mntmCopyErrorMessageToClipboard = new MenuItem(actionsMenu, SWT.NONE);
		mntmCopyErrorMessageToClipboard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyMessageToClipboardActionExecute();
			}
		});
		mntmCopyErrorMessageToClipboard.setEnabled(false);
		mntmCopyErrorMessageToClipboard.setAccelerator(SWT.SHIFT | SWT.CTRL | 'C');
		mntmCopyErrorMessageToClipboard.setText("&Copy Error Message to Clipboard" + "\tShift+Ctrl+C");

		Label menuSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_menuSeparator = new FormData();
		fd_menuSeparator.right = new FormAttachment(100);
		fd_menuSeparator.left = new FormAttachment(0);
		menuSeparator.setLayoutData(fd_menuSeparator);

		toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT | SWT.NO_FOCUS);
		FormData fd_toolBar = new FormData();
		fd_toolBar.top = new FormAttachment(menuSeparator);
		fd_toolBar.right = new FormAttachment(100);
		fd_toolBar.left = new FormAttachment(0);
		toolBar.setLayoutData(fd_toolBar);

		tltmSelectAll = new ToolItem(toolBar, SWT.NONE);
		tltmSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAllActionExecute();
			}
		});
		tltmSelectAll.setToolTipText("Select all tests");
		tltmSelectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/0.png"));

		tltmDeselectAll = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deselectAllActionExecute();
			}
		});
		tltmDeselectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/1.png"));
		tltmDeselectAll.setToolTipText("Deselect all tests");

		new ToolItem(toolBar, SWT.SEPARATOR);

		tltmSelectFailed = new ToolItem(toolBar, SWT.NONE);
		tltmSelectFailed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectFailedActionExecute();
			}
		});
		tltmSelectFailed.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/4.png"));
		tltmSelectFailed.setToolTipText("Select all failed tests");

		new ToolItem(toolBar, SWT.SEPARATOR);

		tltmSelectCurrent = new ToolItem(toolBar, SWT.NONE);
		tltmSelectCurrent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCurrentActionExecute();
			}
		});
		tltmSelectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/2.png"));
		tltmSelectCurrent.setToolTipText("Select current test");

		tltmDeselectCurrent = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectCurrent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deselectCurrentActionExecute();
			}
		});
		tltmDeselectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/3.png"));
		tltmDeselectCurrent.setToolTipText("Deselect current test");

		new ToolItem(toolBar, SWT.SEPARATOR);

		tltmRun = new ToolItem(toolBar, SWT.NONE);
		tltmRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runActionExecute();
			}
		});
		tltmRun.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/7.png"));
		tltmRun.setToolTipText("Run selected tests");

		tltmRunselectedtest = new ToolItem(toolBar, SWT.NONE);
		tltmRunselectedtest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runSelectedTestActionExecute();
			}
		});
		tltmRunselectedtest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		tltmRunselectedtest.setToolTipText("Run current test");

		tltmStop = new ToolItem(toolBar, SWT.NONE);
		tltmStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stopActionExecute();
			}
		});
		tltmStop.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/8.png"));
		tltmStop.setToolTipText("Stop");

		Label toolBarSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_toolBarSeparator = new FormData();
		fd_toolBarSeparator.top = new FormAttachment(toolBar);
		fd_toolBarSeparator.right = new FormAttachment(100);
		fd_toolBarSeparator.left = new FormAttachment(0);
		toolBarSeparator.setLayoutData(fd_toolBarSeparator);

		sashForm = new SashForm(shell, SWT.VERTICAL);
		FormData fd_sashForm = new FormData();
		fd_sashForm.top = new FormAttachment(toolBarSeparator, 4);
		fd_sashForm.right = new FormAttachment(100);
		fd_sashForm.bottom = new FormAttachment(100);
		fd_sashForm.left = new FormAttachment(0);
		sashForm.setLayoutData(fd_sashForm);

		Composite compositeTree = new Composite(sashForm, SWT.NONE);
		compositeTree.setLayout(new FormLayout());

		Label lblTestHierarchy = new Label(compositeTree, SWT.NONE);
		FormData fd_lblTestHierarchy = new FormData();
		fd_lblTestHierarchy.right = new FormAttachment(100);
		fd_lblTestHierarchy.top = new FormAttachment(0);
		fd_lblTestHierarchy.left = new FormAttachment(0);
		lblTestHierarchy.setLayoutData(fd_lblTestHierarchy);
		lblTestHierarchy.setText("Test Hi&erarchy:");

		testTree = new Tree(compositeTree, SWT.BORDER | SWT.CHECK);
		testTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (fRunning && e.keyCode == ' ')
					e.doit = false;
			}
		});
		testTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					TreeItem node = (TreeItem) e.item;
					if (fRunning) {
						node.setChecked(!node.getChecked()); // cancel change
					} else {
						setNodeState(node, node.getChecked());
						runActionUpdate();
					}
					testTree.setSelection(node);
				}
				testTreeChange();
			}
		});
		testTree.setToolTipText("Hierarchy of test cases. Checked test cases will be run.");
		FormData fd_testTree = new FormData();
		fd_testTree.top = new FormAttachment(lblTestHierarchy, 4);
		fd_testTree.left = new FormAttachment(0);
		fd_testTree.bottom = new FormAttachment(100, -3);
		fd_testTree.right = new FormAttachment(100);
		testTree.setLayoutData(fd_testTree);

		TreeItem trtmTest = new TreeItem(testTree, SWT.NONE);
		trtmTest.setChecked(true);
		trtmTest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmTest.setText("Test1");

		TreeItem trtmSubtest = new TreeItem(trtmTest, SWT.NONE);
		trtmSubtest.setChecked(true);
		trtmSubtest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmSubtest.setText("SubTest");

		TreeItem trtmSubtest_1 = new TreeItem(trtmTest, SWT.NONE);
		trtmSubtest_1.setChecked(true);
		trtmSubtest_1.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmSubtest_1.setText("SubTest1.2");
		trtmTest.setExpanded(true);

		TreeItem trtmTest_1 = new TreeItem(testTree, SWT.NONE);
		trtmTest_1.setChecked(true);
		trtmTest_1.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmTest_1.setText("Test2");

		Menu testTreePopupMenu = new Menu(testTree);
		testTreePopupMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				if (testTree.getSelectionCount() > 0) {
					TreeItem node = testTree.getSelection()[0];
					Description test = nodeToTest(node);
					mntmTestCaseProperties.setEnabled(test.isTest());
				}
			}
		});
		testTree.setMenu(testTreePopupMenu);

		mntmTestCaseProperties = new MenuItem(testTreePopupMenu, SWT.NONE);
		mntmTestCaseProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				testCasePropertiesActionExecute();
			}
		});
		mntmTestCaseProperties.setAccelerator(SWT.SHIFT | SWT.CTRL | 'T');
		mntmTestCaseProperties.setText("TestCase Properties" + "\tShift+Ctrl+T");

		new MenuItem(testTreePopupMenu, SWT.SEPARATOR);

		pmntmSelectCurrent = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmSelectCurrent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectCurrentActionExecute();
			}
		});
		pmntmSelectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | 'A');
		pmntmSelectCurrent.setText("Select &Current" + "\tShift+Ctrl+A");

		pmntmDeselectCurrent = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmDeselectCurrent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deselectCurrentActionExecute();
			}
		});
		pmntmDeselectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | SWT.DEL);
		pmntmDeselectCurrent.setText("Deselect C&urrent" + "\tShift+Ctrl+Del");

		new MenuItem(testTreePopupMenu, SWT.SEPARATOR);

		pmntmSelectFailed = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmSelectFailed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectFailedActionExecute();
			}
		});
		pmntmSelectFailed.setAccelerator(SWT.CTRL | 'F');
		pmntmSelectFailed.setText("Select Fai&led" + "\tCtrl+F");

		new MenuItem(testTreePopupMenu, SWT.SEPARATOR);

		pmntmSelectAll = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAllActionExecute();
			}
		});
		pmntmSelectAll.setAccelerator(SWT.CTRL | SWT.ALT | 'A');
		pmntmSelectAll.setText("Select &All" + "\tCtrl+Alt+A");

		pmntmDeselectAll = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmDeselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deselectAllActionExecute();
			}
		});
		pmntmDeselectAll.setAccelerator(SWT.CTRL | SWT.DEL);
		pmntmDeselectAll.setText("&Deselect All" + "\tCtrl+Del");

		new MenuItem(testTreePopupMenu, SWT.SEPARATOR);

		pmntmHideTestNodes = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmHideTestNodes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hideTestNodesActionExecute();
			}
		});
		pmntmHideTestNodes.setAccelerator(SWT.CTRL | 'H');
		pmntmHideTestNodes.setText("&Hide Test Nodes" + "\tCtrl+H");

		pmntmExpandAll = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmExpandAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				expandAllNodesActionExecute();
			}
		});
		pmntmExpandAll.setAccelerator(SWT.CTRL | 'P');
		pmntmExpandAll.setText("Ex&pand All" + "\tCtrl+P");

		new MenuItem(testTreePopupMenu, SWT.SEPARATOR);

		MenuItem pmntmGoToNextSelectedNode = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmGoToNextSelectedNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goToNextSelectedTestActionExecute();
			}
		});
		pmntmGoToNextSelectedNode.setAccelerator(SWT.CTRL | SWT.ARROW_RIGHT);
		pmntmGoToNextSelectedNode.setText("Go To Next Selected Test" + "\tCtrl+Right");

		MenuItem pmntmGoToPreviousSelectedNode = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmGoToPreviousSelectedNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goToPrevSelectedTestActionExecute();
			}
		});
		pmntmGoToPreviousSelectedNode.setAccelerator(SWT.CTRL | SWT.ARROW_LEFT);
		pmntmGoToPreviousSelectedNode.setText("Go To Previous Selected Test" + "\tCtrl+Left");

		new MenuItem(testTreePopupMenu, SWT.SEPARATOR);

		pmntmCopyTestnameToClipboard = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmCopyTestnameToClipboard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyTestnameToClipboardActionExecute();
			}
		});
		pmntmCopyTestnameToClipboard.setAccelerator(SWT.CTRL | SWT.ALT | 'C');
		pmntmCopyTestnameToClipboard.setText("Copy testname to clipboard" + "\tCtrl+Alt+C");

		new MenuItem(testTreePopupMenu, SWT.SEPARATOR);

		pmntmRunSelectedTest = new MenuItem(testTreePopupMenu, SWT.NONE);
		pmntmRunSelectedTest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runSelectedTestActionExecute();
			}
		});
		pmntmRunSelectedTest.setAccelerator(SWT.F8);
		pmntmRunSelectedTest.setText("Run selected test" + "\tF8");

		compositeResults = new Composite(sashForm, SWT.NONE);
		compositeResults.setLayout(new FormLayout());

		Composite compositeProgressAndScore = new Composite(compositeResults, SWT.BORDER);
		compositeProgressAndScore.setToolTipText("");
		compositeProgressAndScore.setLayout(new FormLayout());
		FormData fd_compositeProgressAndScore = new FormData();
		fd_compositeProgressAndScore.bottom = new FormAttachment(0, 52);
		fd_compositeProgressAndScore.right = new FormAttachment(100);
		fd_compositeProgressAndScore.top = new FormAttachment(0, 3);
		fd_compositeProgressAndScore.left = new FormAttachment(0);
		compositeProgressAndScore.setLayoutData(fd_compositeProgressAndScore);

		Label lblProgress = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblProgress = new FormData();
		fd_lblProgress.right = new FormAttachment(0, 59);
		fd_lblProgress.top = new FormAttachment(0, 6);
		fd_lblProgress.left = new FormAttachment(0, 10);
		lblProgress.setLayoutData(fd_lblProgress);
		lblProgress.setText("Progress:");

		progressBar = new ProgressBar(compositeProgressAndScore, SWT.NONE);
		progressBar.setToolTipText("Shows the proportion of tests run");
		FormData fd_progressBar = new FormData();
		fd_progressBar.height = 12;
		fd_progressBar.top = new FormAttachment(0, 6);
		fd_progressBar.left = new FormAttachment(lblProgress, 6);
		fd_progressBar.right = new FormAttachment(100, -6);
		progressBar.setLayoutData(fd_progressBar);

		Label lblScore = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblScore = new FormData();
		fd_lblScore.right = new FormAttachment(lblProgress, 0, SWT.RIGHT);
		fd_lblScore.top = new FormAttachment(lblProgress, 6);
		fd_lblScore.left = new FormAttachment(lblProgress, 0, SWT.LEFT);
		lblScore.setLayoutData(fd_lblScore);
		lblScore.setText("Score:");

		scoreBar = new ProgressBar(compositeProgressAndScore, SWT.BORDER);
		scoreBar.setToolTipText("Shows the proportion of successful tests");
		FormData fd_scoreBar = new FormData();
		fd_scoreBar.right = new FormAttachment(100, -56);
		fd_scoreBar.height = 12;
		fd_scoreBar.top = new FormAttachment(progressBar, 6);
		fd_scoreBar.left = new FormAttachment(progressBar, 0, SWT.LEFT);
		scoreBar.setLayoutData(fd_scoreBar);

		lblProgressPercent = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblProgressPercent = new FormData();
		fd_lblProgressPercent.top = new FormAttachment(lblScore, 0, SWT.TOP);
		fd_lblProgressPercent.left = new FormAttachment(scoreBar, 6);
		fd_lblProgressPercent.right = new FormAttachment(100, -6);
		lblProgressPercent.setLayoutData(fd_lblProgressPercent);
		lblProgressPercent.setText("Progress");

		tableResults = new Table(compositeResults, SWT.BORDER | SWT.HIDE_SELECTION);
		tableResults.setToolTipText("Shows statistics about the current/last run");
		FormData fd_tableResults = new FormData();
		fd_tableResults.height = 16;
		fd_tableResults.top = new FormAttachment(compositeProgressAndScore, 3);
		fd_tableResults.right = new FormAttachment(100, 0);
		fd_tableResults.left = new FormAttachment(0, 0);
		tableResults.setLayoutData(fd_tableResults);
		tableResults.setHeaderVisible(true);

		TableColumn tableColumn = new TableColumn(tableResults, SWT.NONE);
		tableColumn.setWidth(8);

		TableColumn tblclmnTests = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTests.setWidth(64);
		tblclmnTests.setText("Tests");

		TableColumn tblclmnRun = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnRun.setWidth(64);
		tblclmnRun.setText("Run");

		TableColumn tblclmnFailures = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnFailures.setWidth(64);
		tblclmnFailures.setText("Failures");

		TableColumn tblclmnSkipped = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnSkipped.setWidth(64);
		tblclmnSkipped.setText("Skipped");

		TableColumn tblclmnIgnored = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnIgnored.setWidth(64);
		tblclmnIgnored.setText("Ignored");

		TableColumn tblclmnTestTime = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTestTime.setWidth(76);
		tblclmnTestTime.setText("Test Time");

		TableColumn tblclmnTotalTime = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTotalTime.setWidth(76);
		tblclmnTotalTime.setText("Total Time");

		TableItem tableItem = new TableItem(tableResults, SWT.NONE);
		tableItem.setText(new String[] {"", "1", "2", "3", "4", "5", "6", "7"});

		tableFailureList = new Table(compositeResults, SWT.BORDER | SWT.FULL_SELECTION);
		tableFailureList.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int columnsWidth = 0;
				for (int i = 0; i < tableFailureList.getColumnCount() - 1; i++)
					columnsWidth += tableFailureList.getColumn(i).getWidth();
				TableColumn lastColumn = tableFailureList.getColumn(tableFailureList.getColumnCount() - 1);
				lastColumn.setWidth(Math.max(tableFailureList.getClientArea().width - columnsWidth, 60));
			}
		});
		tableFailureList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tableFailureList.getSelectionIndex() != -1) {
					TableItem item = tableFailureList.getItem(tableFailureList.getSelectionIndex());
					TreeItem node = (TreeItem) item.getData();
					if (testTree.getSelectionCount() == 0 || testTree.getSelection()[0] != node) {
						testTree.setSelection(node);
						testTreeChange();
					} else {
						displayFailureMessage(item);
					}
				} else {
					clearFailureMessage();
				}
				copyMessageToClipboardActionUpdate();
			}
		});
		tableFailureList.setToolTipText("Shows the list of failed tests");
		FormData fd_tableFailureList = new FormData();
		fd_tableFailureList.bottom = new FormAttachment(100, -3);
		fd_tableFailureList.left = new FormAttachment(0, 0);
		fd_tableFailureList.right = new FormAttachment(100, 0);
		fd_tableFailureList.top = new FormAttachment(tableResults, 3);
		tableFailureList.setLayoutData(fd_tableFailureList);
		tableFailureList.setHeaderVisible(true);

		TableColumn tblclmnTestName = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnTestName.setWidth(120);
		tblclmnTestName.setText("Test Name");

		TableColumn tblclmnFailureType = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnFailureType.setWidth(100);
		tblclmnFailureType.setText("Failure Type");

		TableColumn tblclmnMessage = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnMessage.setWidth(200);
		tblclmnMessage.setText("Message");

		TableColumn tblclmnLocation = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnLocation.setWidth(60);
		tblclmnLocation.setText("Location");

		compositeErrorBox = new Composite(sashForm, SWT.NONE);
		compositeErrorBox.setLayout(new FormLayout());

		errorMessageStyledText = new StyledText(compositeErrorBox, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		errorMessageStyledText.setEditable(false);
		errorMessageStyledText.setAlwaysShowScrollBars(false);
		errorMessageStyledText.setText("ErrorMessageRTF");
		FormData fd_errorMessageStyledText = new FormData();
		fd_errorMessageStyledText.bottom = new FormAttachment(100, -3);
		fd_errorMessageStyledText.right = new FormAttachment(100);
		fd_errorMessageStyledText.top = new FormAttachment(0, 3);
		fd_errorMessageStyledText.left = new FormAttachment(0);
		errorMessageStyledText.setLayoutData(fd_errorMessageStyledText);

		Menu errorMessagePopupMenu = new Menu(errorMessageStyledText);
		errorMessageStyledText.setMenu(errorMessagePopupMenu);

		pmntmCopyErrorMessageToClipboard = new MenuItem(errorMessagePopupMenu, SWT.NONE);
		pmntmCopyErrorMessageToClipboard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyMessageToClipboardActionExecute();
			}
		});
		pmntmCopyErrorMessageToClipboard.setEnabled(false);
		pmntmCopyErrorMessageToClipboard.setAccelerator(SWT.SHIFT | SWT.CTRL | 'C');
		pmntmCopyErrorMessageToClipboard.setText("&Copy Error Message to Clipboard" + "\tShift+Ctrl+C");

		sashForm.setWeights(new int[] {200, 150, 50});
	}

	void formCreate() {
		fTests = new ArrayList<>();
		testToNodeMap = new HashMap<>();
		fTestTimeMap = new HashMap<Description, TestTime>();
		loadConfiguration();

		setUpStateImages();
		setupCustomShortcuts();
		testTree.removeAll(); // XXX unnecessary action
		enableUI(false);
		clearFailureMessage();
		setup();

		mntmFailTestCaseIfMemoryLeaked.setEnabled(false);
		mntmReportMemoryLeakTypeOnShutdown.setSelection(false);
		mntmReportMemoryLeakTypeOnShutdown.setEnabled(false);

		if (!mntmFailTestCaseIfMemoryLeaked.isEnabled())
			mntmFailTestCaseIfMemoryLeaked.setSelection(false);
		mntmIgnoreMemoryLeakInSetUpTearDown.setEnabled(mntmFailTestCaseIfMemoryLeaked.getSelection());
		if (!mntmIgnoreMemoryLeakInSetUpTearDown.getEnabled())
			mntmIgnoreMemoryLeakInSetUpTearDown.setSelection(false);

		runActionUpdate();
		copyMessageToClipboardActionUpdate();
		stopActionUpdate();
	}

	void formDestroy() {
		clearResult();
		autoSaveConfiguration();
		fSuite = null;
		fTests = null;
	}

	void formShow() {
		setupGuiNodes();
	}

	void selectAllActionExecute() {
		for (TreeItem rootNode: testTree.getItems())
			applyToTests(rootNode, new EnableTest());
		updateStatus(true);
		runActionUpdate();
	}

	void deselectAllActionExecute() {
		for (TreeItem rootNode: testTree.getItems())
			applyToTests(rootNode, new DisableTest());
		updateStatus(true);
		runActionUpdate();
	}

	void selectFailedActionExecute() {
		// deselect all
		for (TreeItem rootNode: testTree.getItems())
			applyToTests(rootNode, new DisableTest());

		// select failed
		for (TableItem item: tableFailureList.getItems()) {
			TreeItem node = (TreeItem) item.getData();
			setNodeState(node, true);
		}
		updateStatus(true);
		runActionUpdate();
	}

	void selectCurrentActionExecute() {
		if (testTree.getSelectionCount() > 0) {
			applyToTests(testTree.getSelection()[0], new EnableTest());
			setNodeState(testTree.getSelection()[0], true);
			updateStatus(true);
			runActionUpdate();
		}
	}

	void deselectCurrentActionExecute() {
		if (testTree.getSelectionCount() > 0) {
			applyToTests(testTree.getSelection()[0], new DisableTest());
			updateStatus(true);
			runActionUpdate();
		}
	}

	private List<TreeItem> getAllTestTreeItemsOrderly() {
		List<TreeItem> result = new ArrayList<>();
		for (TreeItem node : testTree.getItems()) {
			result.add(node);
			result.addAll(getAllTestTreeItemsOrderly(node));
		}
		return result;
	}

	private List<TreeItem> getAllTestTreeItemsOrderly(TreeItem node) {
		List<TreeItem> result = new ArrayList<>();
		for (TreeItem childNode : node.getItems()) {
			result.add(childNode);
			result.addAll(getAllTestTreeItemsOrderly(childNode));
		}
		return result;
	}

	private void fullExpandTestTree() {
		for (TreeItem node: getAllTestTreeItemsOrderly())
			node.setExpanded(true);
	}

	void hideTestNodesActionExecute() {
		if (testTree.getItemCount() == 0)
			return;

		testTree.setRedraw(false);
		try {
			fullExpandTestTree();
			for (TreeItem node: testTree.getItems())
				collapseNonGrandparentNodes(node);
			selectNode(testTree.getItem(0));
		} finally {
			testTree.setRedraw(true);
			testTree.update();
		}
	}

	void expandAllNodesActionExecute() {
		fullExpandTestTree();
		if (testTree.getSelectionCount() > 0)
			testTree.showSelection();
		else if (testTree.getItemCount() > 0) {
			testTree.setSelection(testTree.getItem(0));
			testTreeChange();
		}
	}

	void runActionExecute() {
		if (fSuite == null)
			return;

		setup();
		runTheTest();
	}

	void runActionUpdate() {
		boolean enabled = !fRunning && fSuite!= null && countEnabledTestCases() > 0;
		mntmRun.setEnabled(enabled);
		tltmRun.setEnabled(enabled);
	}

	void copyMessageToClipboardActionExecute() {
		errorMessageStyledText.selectAll();
		errorMessageStyledText.copy();
	}

	void copyMessageToClipboardActionUpdate() {
		boolean enabled = tableFailureList.getSelectionIndex() != -1;
		mntmCopyErrorMessageToClipboard.setEnabled(enabled);
		pmntmCopyErrorMessageToClipboard.setEnabled(enabled);
	}

	void stopActionExecute() {
		if (fTestResult != null && fNotifier != null)
			fNotifier.pleaseStop();
	}

	void stopActionUpdate() {
		boolean enabled = fRunning && fTestResult != null && fNotifier != null;
		mntmStop.setEnabled(enabled);
		tltmStop.setEnabled(enabled);
	}

	void testTreeChange() {
		if (testTree.getSelectionCount() > 0) {
			TreeItem node = testTree.getSelection()[0];
			tableFailureList.deselectAll();
			clearFailureMessage();
			for (int i = 0; i < tableFailureList.getItemCount(); ++i) {
				if (tableFailureList.getItem(i).getData() == node) {
					tableFailureList.select(i); // XXX no showSelection()
					displayFailureMessage(tableFailureList.getItem(i));
					break;
				}
			}
			updateStatus(true);
			copyMessageToClipboardActionUpdate();
		}
		copyTestnameToClipboardActionUpdate();
		runSelectedTestActionUpdate();
	}

	void copyTestnameToClipboardActionExecute() {
		if (testTree.getSelectionCount() > 0) {
			Clipboard clipboard = new Clipboard(Display.getCurrent());
			clipboard.setContents(new Object[] { testTree.getSelection()[0].getText() },
					new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
		}
	}

	void copyTestnameToClipboardActionUpdate() {
		Description selectedTest = selectedTest();
		Boolean enabled = selectedTest == null ? false : selectedTest.isTest();
		mntmCopyTestnameToClipboard.setEnabled(enabled);
		pmntmCopyTestnameToClipboard.setEnabled(enabled);
	}

	void runSelectedTestActionExecute() {
		setup();
		listSelectedTests();
		progressBar.setMaximum(1);
		scoreBar.setMaximum(1);
		runTheTest();
		fSelectedTests = null;
	}

	void runSelectedTestActionUpdate() {
		Description selectedTest = selectedTest();
		Boolean enabled = selectedTest == null ? false : selectedTest.isTest();
		mntmRunSelectedTest.setEnabled(enabled);
		mntmRunSelectedTest2.setEnabled(enabled);
		tltmRunselectedtest.setEnabled(enabled);
		pmntmRunSelectedTest.setEnabled(enabled);
	}

	void goToNextSelectedTestActionExecute() {
		if (testTree.getSelectionCount() > 0) {
			List<TreeItem> allNodes = getAllTestTreeItemsOrderly();
			int nodeIndex = allNodes.indexOf(testTree.getSelection()[0]);
			while (++nodeIndex < allNodes.size()) {
				if (selectNodeIfTestEnabled(allNodes.get(nodeIndex)))
					break;
			}
		}
	}

	void goToPrevSelectedTestActionExecute() {
		if (testTree.getSelectionCount() > 0) {
			List<TreeItem> allNodes = getAllTestTreeItemsOrderly();
			int nodeIndex = allNodes.indexOf(testTree.getSelection()[0]);
			while (--nodeIndex >= 0) {
				if (selectNodeIfTestEnabled(allNodes.get(nodeIndex)))
					break;
			}
		}
	}

	void testCasePropertiesActionExecute() {
		if (testTree.getSelectionCount() > 0) {
			TreeItem node = testTree.getSelection()[0];
			Description test = nodeToTest(node);
			if (test.isTest()) {
				Menu testCasePropertyPopupMenu = new Menu(shell, SWT.POP_UP);
				MenuItem ppmntmHeader = new MenuItem(testCasePropertyPopupMenu, SWT.NONE);
				ppmntmHeader.setText("TestCase Properties");
				ppmntmHeader.setEnabled(false);
				new MenuItem(testCasePropertyPopupMenu, SWT.SEPARATOR);
				MenuItem ppmntmPrevious = new MenuItem(testCasePropertyPopupMenu, SWT.NONE);
				ppmntmPrevious.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						goToPrevSelectedTestActionExecute();
						testCasePropertiesActionExecute();
					}
				});
				ppmntmPrevious.setText("Previous");
				MenuItem ppmntmRunSelectedTest = new MenuItem(testCasePropertyPopupMenu, SWT.NONE);
				ppmntmRunSelectedTest.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						runSelectedTestAltActionExecute();
					}
				});
				ppmntmRunSelectedTest.setText("Run Selected Test");
				MenuItem ppmntmNext = new MenuItem(testCasePropertyPopupMenu, SWT.NONE);
				ppmntmNext.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						goToNextSelectedTestActionExecute();
						testCasePropertiesActionExecute();
					}
				});
				ppmntmNext.setText("Next");
				new MenuItem(testCasePropertyPopupMenu, SWT.SEPARATOR);
				for(Annotation annotation: test.getAnnotations()) {
					MenuItem ppmntmAnnotation = new MenuItem(testCasePropertyPopupMenu, SWT.NONE);
					ppmntmAnnotation.setText(annotation.toString());
					ppmntmAnnotation.setEnabled(false);
				}
				testCasePropertyPopupMenu.setLocation(shell.getLocation().x + fPopupX, shell.getLocation().y + fPopupY);
				testCasePropertyPopupMenu.setVisible(true);
			}
		}
	}

	void runSelectedTestAltActionExecute() {
		runSelectedTestActionExecute();
		testCasePropertiesActionExecute();
	}

	void resetProgress() {
		scoreBar.setBackground(null);
		scoreBar.update();
		scoreBar.setSelection(0);
		progressBar.setSelection(0);
		lblProgressPercent.setText("");
	}

	protected static class TestTime {
		private long startTime;
		private long endTime;
		public TestTime() {
			startTime = System.currentTimeMillis();
			endTime = 0;
		}
		public void stop() {
			endTime = System.currentTimeMillis();
		}
		public long getElapsed() {
			return (endTime != 0 ? endTime : System.currentTimeMillis()) - startTime;
		}
	}

	private int fPopupX;
	private int fPopupY;

	protected Class<?> fSuite;
	protected Result fTestResult;
	protected RunNotifier fNotifier;
	protected int fAssumptionFailureCount;
	protected boolean fRunning;
	protected ArrayList<Description> fTests;
	protected Map<Description, TreeItem> testToNodeMap;
	protected List<Description> fSelectedTests;
	protected long fTotalTime;
	protected Map<Description, TestTime> fTestTimeMap;
	protected int fFailureCount;
	protected int fIgnoreCount;
	protected int fTotalTestCount;

	protected void setup() {
		tableFailureList.removeAll();
		resetProgress();
		shell.update();

		TableItem item = tableResults.getItem(0);
		if (fSuite != null) {
			int i = countEnabledTestCases();
			item.setText(1, Integer.toString(i));
			progressBar.setMaximum(i);
		} else {
			item.setText(1, "");
			progressBar.setMaximum(10000);
		}
		scoreBar.setMaximum(progressBar.getMaximum());

		item.setText(2, "");
		item.setText(3, "");
		item.setText(4, "");
		item.setText(5, "");
		item.setText(6, "");
		item.setText(7, "");

		for (TreeItem node: getAllTestTreeItemsOrderly()) {
			node.setImage(imgNone);
		}
		updateTestTreeState(); // XXX there is no need any test condition is not changed
	}

	protected void setUpStateImages() {
		imgNone = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png");
		imgRunning = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/1.png");
		imgRun = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/2.png");
		imgHasProps = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/3.png");
		imgFailed = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/4.png");
		imgError = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/5.png");
		indexesImages = new HashMap<>();
		indexesImages.put(imgNone, 0);
		indexesImages.put(imgRunning, 1);
		indexesImages.put(imgRun, 2);
		indexesImages.put(imgHasProps, 3);
		indexesImages.put(imgFailed, 4);
		indexesImages.put(imgError, 5);
		initColors();
	}

	private void initColors() {
		clOk = display.getSystemColor(SWT.COLOR_GREEN);
		clFailure = display.getSystemColor(SWT.COLOR_MAGENTA);
		clError = display.getSystemColor(SWT.COLOR_RED);
	}

	protected void setSuite(Class<?> value) {
		fSuite = value;
		if (fSuite != null) {
			//loadSuiteConfiguration(); // moved into initTree()
			enableUI(true);
			initTree();
		} else {
			enableUI(false);
		}
		testTreeChange();
		runActionUpdate();
	}

	protected void clearResult() {
		if (fTestResult != null || fNotifier != null) {
			fTestResult = null;
			fNotifier = null;
			clearFailureMessage();
			stopActionUpdate();
		}
	}

	protected void displayFailureMessage(TableItem item) {
		Color hlColor = clFailure;
		if (indexesImages.get(item.getImage()).intValue() >= indexesImages.get(imgError).intValue())
			hlColor = clError;

		errorMessageStyledText.setText("");

		int start = 0;
		errorMessageStyledText.setSelection(start);
		errorMessageStyledText.insert(item.getText() + ": ");
		int end = errorMessageStyledText.getText().length();
		StyleRange range = new StyleRange();
		range.start = start;
		range.length = end - start;
		range.fontStyle = SWT.BOLD;
		errorMessageStyledText.setStyleRange(range);

		start = end;
		errorMessageStyledText.setSelection(start);
		errorMessageStyledText.insert(item.getText(1));
		end = errorMessageStyledText.getText().length();
		range = new StyleRange();
		range.start = start;
		range.length = end - start;
		range.foreground = hlColor;
		range.fontStyle = SWT.BOLD;
		errorMessageStyledText.setStyleRange(range);

		start = end;
		errorMessageStyledText.setSelection(start);
		errorMessageStyledText.insert("\r\nat " + item.getText(3));
		end = errorMessageStyledText.getText().length();

		FontData fontData = errorMessageStyledText.getFont().getFontData()[0];

		if (!item.getText(2).isEmpty()) {
			start = end;
			errorMessageStyledText.setSelection(start);
			errorMessageStyledText.insert("\r\n" + item.getText(2));
			end = errorMessageStyledText.getText().length();
			range = new StyleRange();
			range.start = start;
			range.length = end - start;
			range.font = new Font(display, fontData.getName(), 12, fontData.getStyle());
			errorMessageStyledText.setStyleRange(range);
		}

		String stack = (String) item.getData("stack");
		if (stack != null && !stack.isEmpty()) {
			start = end;
			errorMessageStyledText.setSelection(start);
			errorMessageStyledText.insert("\r\nStackTrace\r\n");
			end = errorMessageStyledText.getText().length();
			range = new StyleRange();
			range.start = start;
			range.length = end - start;
			range.fontStyle = SWT.BOLD;
			errorMessageStyledText.setStyleRange(range);

			start = end;
			errorMessageStyledText.setSelection(start);
			errorMessageStyledText.insert(stack);
			end = errorMessageStyledText.getText().length();
		}

		errorMessageStyledText.setSelection(0);
	}

	protected void clearFailureMessage() {
		errorMessageStyledText.setText("");
	}


	protected TableItem addFailureItem(Failure failure) {
		assert failure != null;
		TableItem item = new TableItem(tableFailureList, SWT.NONE);
		item.setData(testToNode(failure.getDescription()));
		item.setText(getShortDisplayName(failure.getDescription()));
		int column = 1;
		if (failure.getException() != null) {
			item.setText(column++, failure.getException().getClass().getSimpleName());
			if (failure.getException().getMessage() != null)
				item.setText(column++, failure.getException().getMessage());
			else
				item.setText(column++, "");
			item.setText(column++, getLocationFromException(failure.getException()));
			item.setData("stack", failure.getTrace());;
		}

		TreeItem node = testToNode(failure.getDescription());
		while (node != null) {
			node.setExpanded(true);
			node = node.getParentItem();
		}

		return item;
	}

	private String getLocationFromException(Throwable exception) {
		if (exception == null || exception.getStackTrace() == null)
			return "";
		StackTraceElement[] stack = exception.getStackTrace();
		for (StackTraceElement element: stack)
			if (!filterLine(element.toString()))
				return (element.getFileName() != null && element.getLineNumber() >= 0
						? element.getFileName() + ":" + element.getLineNumber()
						: element.getFileName() != null ? element.getFileName() : "(Unknown Source)")
						+ " " + element.getClassName() + "." + element.getMethodName()
						+ (element.isNativeMethod() ? "(Native Method)" : "");
		return "";
	}

	private static final String[] STACK_FILTER_PATTERNS = new String[] {
			"junit.framework.TestCase.",
			"junit.framework.TestResult.",
			"junit.framework.TestResult$1.",
			"junit.framework.TestSuite.",
			"junit.framework.Assert.",
			"org.junit.",
			"java.lang.reflect.Method.invoke",
			"sun.reflect.",
	};
	private MenuItem mntmTestCaseProperties;

	private boolean filterLine(String line) {
		for (String pattern: STACK_FILTER_PATTERNS)
			if (line.indexOf(pattern) >= 0)
				return true;
		return false;
	}

	private String formatElapsedTime(long milli) {
		long h = milli / 3600000;
		milli = milli % 3600000;
		long nn = milli / 60000;
		milli = milli % 60000;
		long ss = milli / 1000;
		milli = milli % 1000;
		long zzz = milli;
		return String.format("%d:%02d:%02d.%03d", h, nn, ss, zzz);
	}

	protected void updateStatus(boolean fullUpdate) {
		if (tableResults.getItemCount() == 0)
			return;

		String prevTotal = tableResults.getItem(0).getText(1);

		if (fullUpdate) {
			fTotalTestCount = countEnabledTestCases();
			if (fSuite != null)
				tableResults.getItem(0).setText(1, Integer.toString(fTotalTestCount));
			else
				tableResults.getItem(0).setText(1, "");
		}

		if (fTestResult != null) {
			// Save the test number as we use it a lot
			int testNumber = fTestResult.getRunCount();

			if (fullUpdate || (testNumber & 15) == 0) {
				TableItem item = tableResults.getItem(0);
				item.setText(2, Integer.toString(testNumber));
				item.setText(3, Integer.toString(fTestResult.getFailureCount()));
				item.setText(4, Integer.toString(fAssumptionFailureCount));
				item.setText(5, Integer.toString(fTestResult.getIgnoreCount()));
				item.setText(6, formatElapsedTime(fTestResult.getRunTime()));
				item.setText(7, formatElapsedTime(Math.max(fTestResult.getRunTime(), fTotalTime)));

				scoreBar.setSelection(testNumber - fTestResult.getFailureCount());
				progressBar.setSelection(testNumber + fTestResult.getIgnoreCount());

				// There is a possibility for zero tests
				if (testNumber == 0 && fTotalTestCount == 0)
					lblProgressPercent.setText("100%");
				else
					lblProgressPercent.setText(Integer.toString(100 * scoreBar.getSelection() / scoreBar.getMaximum()) + "%");
			}
			// Allow just the results pane to catch up

			compositeResults.update();
		} else {
			TableItem item = tableResults.getItem(0);
			if (item.getText(1).equals("0") || item.getText(1).equals("")) {
				for (int i = 2; i <= 7; ++i)
					item.setText(i, "");
			} else if (!item.getText(1).equals(prevTotal) || item.getText(2).equals("")) {
				for (int i = 2; i <= 7; ++i)
					item.setText(i, "");
			} else {
				item.setText(6, formatElapsedTime(getElapsedTestTime(selectedTest())));
				item.setText(7, formatElapsedTime(Math.max(getElapsedTestTime(selectedTest()), fTotalTime)));
			}

			resetProgress();
		}

		if (fullUpdate) {
			// Allow the whole display to catch up and check for key strokes

			shell.update();
			processMessages();
		}
	}


	protected void fillTestTree() {
		testTree.removeAll();
		fTests.clear();
		testToNodeMap.clear();
		fTestTimeMap.clear();

		Request request = Request.aClass(fSuite);
		Runner runner = request.getRunner();
		Description rootDescription = runner.getDescription();

		TreeItem rootItem = new TreeItem(testTree, SWT.NONE);

		fillTestTree(rootItem, rootDescription);
	}

	protected void fillTestTree(TreeItem treeItem, Description description) {
		if (description == null)
			return;

		treeItem.setText(getShortDisplayName(description));
		treeItem.setData(fTests.size());

		fTests.add(description);
		testToNodeMap.put(description, treeItem);

		for (Description childDescripton: description.getChildren()) {
			fillTestTree(new TreeItem(treeItem, SWT.NONE), childDescripton);
		}
	}


	protected void updateNodeImage(TreeItem node) {
		if (node.getChecked() && node.getParentItem() != null &&
				(!node.getParentItem().getChecked() || node.getParentItem().getGrayed())) {
			node.setGrayed(true);
		} else {
			node.setGrayed(false);
		}
	}

	protected void updateNodeState(TreeItem node) {
		assert node != null;
		Description test = nodeToTest(node);
		assert test != null;

		updateNodeImage(node);

		for (TreeItem childNode: node.getItems()) {
			updateNodeState(childNode);
		}
	}

	protected void setNodeState(TreeItem node, boolean enabled) {
		assert node != null;

		// update ancestors if enabling
		node.setChecked(enabled);

		TreeItem mostSeniorChanged = node;
		if (enabled) {
			while(node.getParentItem() != null) {
				node = node.getParentItem();
				if (!node.getChecked()) {
					// changed
					node.setChecked(true);
					mostSeniorChanged = node;
					updateNodeImage(node);
				}
			}
		}
		testTree.setRedraw(false);
		try {
			updateNodeState(mostSeniorChanged);
		} finally {
			testTree.setRedraw(true);
			testTree.update();
		}
	}

	protected void updateTestTreeState() {
		if (testTree.getItemCount() > 0) {
			testTree.setRedraw(false);
			try {
				for (TreeItem node: testTree.getItems()) {
					updateNodeState(node);
				}
			} finally {
				testTree.setRedraw(true);
				testTree.update();
			}
		}
	}


	protected void makeNodeVisible(TreeItem node) {
		node.getParent().showItem(node);
	}

	protected void setTreeNodeImage(TreeItem node, Image image) {
		while (node != null) {
			if (indexesImages.get(image).intValue() > indexesImages.get(node.getImage()).intValue()) {
				node.setImage(image);
			}
			if (image == imgRunning)
				node = null;
			else
				node = node.getParentItem();
		}
	}

	protected void selectNode(TreeItem node) {
		node.getParent().select(node);
		makeNodeVisible(node);
		testTreeChange();
	}


	protected Description nodeToTest(TreeItem node) {
		assert node != null;

		int idx = (int) node.getData();
		assert idx >=0 && idx < fTests.size();
		return fTests.get(idx);
	}

	protected TreeItem testToNode(Description test) {
		return testToNodeMap.get(test);
	}

	protected Description selectedTest() {
		if (testTree.getSelectionCount() == 0)
			return null;
		else
			return nodeToTest(testTree.getSelection()[0]);
	}

	protected void listSelectedTests() {
		fSelectedTests = new ArrayList<>();

		TreeItem aNode = testTree.getSelectionCount() > 0 ? testTree.getSelection()[0] : null;

		while (aNode != null) {
			Description aTest = nodeToTest(aNode);
			fSelectedTests.add(aTest);
			aNode = aNode.getParentItem();
		}
	}

	protected static interface ITestFunc {
		boolean exec(TreeItem item);
	}

	protected static class EnableTest implements ITestFunc {
		@Override
		public boolean exec(TreeItem item) {
			item.setChecked(true);
			return true;
		}
	}

	protected static class DisableTest implements ITestFunc {
		@Override
		public boolean exec(TreeItem item) {
			item.setChecked(false);
			return true;
		}
	}

	protected void applyToTests(TreeItem root, final ITestFunc func) {
		testTree.setRedraw(false);
		try {
			doApply(root, func);
		} finally {
			testTree.setRedraw(true);
			testTree.update();
		}
		updateTestTreeState();
	}

	private void doApply(TreeItem rootnode, ITestFunc func) {
		if (rootnode != null) {
			if (func.exec(rootnode)) {
				for (TreeItem node: rootnode.getItems()) {
					doApply(node, func);
				}
			}
		}
	}

	protected void enableUI(boolean enable) {
		mntmSelectAll.setEnabled(enable);
		tltmSelectAll.setEnabled(enable);
		pmntmSelectAll.setEnabled(enable);
		mntmDeselectAll.setEnabled(enable);
		tltmDeselectAll.setEnabled(enable);
		pmntmDeselectAll.setEnabled(enable);
		mntmSelectFailed.setEnabled(enable);
		tltmSelectFailed.setEnabled(enable);
		pmntmSelectFailed.setEnabled(enable);
		mntmSelectCurrent.setEnabled(enable);
		tltmSelectCurrent.setEnabled(enable);
		pmntmSelectCurrent.setEnabled(enable);
		mntmDeselectCurrent.setEnabled(enable);
		tltmDeselectCurrent.setEnabled(enable);
		pmntmDeselectCurrent.setEnabled(enable);
		mntmHideTestNodes.setEnabled(enable);
		pmntmHideTestNodes.setEnabled(enable);
		mntmExpandAll.setEnabled(enable);
		pmntmExpandAll.setEnabled(enable);
	}

	protected void runTheTest() {
		if (fSuite == null)
			return;
		if (fRunning) {
			// warning: we're reentering this method if fRunning is true
			assert fTestResult != null && fNotifier != null;
			fNotifier.pleaseStop();
			return;
		}

		fRunning = true;
		try {
			mntmRun.setEnabled(false);
			tltmRun.setEnabled(false);
			mntmStop.setEnabled(true);
			tltmStop.setEnabled(true);

			mntmCopyErrorMessageToClipboard.setEnabled(false);
			pmntmCopyErrorMessageToClipboard.setEnabled(false);

			enableUI(false);
			autoSaveConfiguration();
			clearResult();

			// See: org.junit.runner.JUnitCore.run(Runner runner)
			// See: org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(TestExecution execution)
			Request request = Request.aClass(fSuite).filterWith(new RunTheTestFilter());
			Runner runner = request.getRunner();
			fTestResult = new Result();
			fNotifier = new RunNotifier(); // reuse is impossible due pleaseStop member
			fAssumptionFailureCount = 0;
			RunListener resultListener = fTestResult.createListener();
			fNotifier.addFirstListener(resultListener);
			RunListener runTheTestListener = new RunTheTestListener();
			fNotifier.addListener(runTheTestListener);
			try {
				// TODO TestResult.FailsIfNoChecksExecuted := FailIfNoChecksExecutedAction.Checked;
				// TODO TestResult.FailsIfMemoryLeaked := FailTestCaseIfMemoryLeakedAction.Checked;
				// TODO TestResult.IgnoresMemoryLeakInSetUpTearDown := IgnoreMemoryLeakInSetUpTearDownAction.Checked;
				fNotifier.fireTestRunStarted(runner.getDescription());
				runner.run(fNotifier);
				fNotifier.fireTestRunFinished(fTestResult);
			} catch (StoppedByUserException e) {
				// not interesting
			} finally {
				fNotifier.removeListener(runTheTestListener);
				fNotifier.removeListener(resultListener);
				fIgnoreCount = fTestResult.getIgnoreCount();
				fFailureCount = fTestResult.getFailureCount();
				fTestResult = null;
				fNotifier = null;
			}
		} finally {
			fRunning = false;
			enableUI(true);
			runActionUpdate();
			stopActionUpdate();
		}
	}


	protected void initTree() {
		//fTests.clear();
		fillTestTree(/*suite*/);
		loadSuiteConfiguration(); // moved from setSuite()
		setup();
		if (mntmHideTestNodesOnOpen.getSelection()) {
			hideTestNodesActionExecute();
		} else {
			expandAllNodesActionExecute();
		}
		if (testTree.getItemCount() > 0)
			testTree.setSelection(testTree.getItem(0));
	}

	protected String iniFileName() {
		return new File(System.getProperty("user.dir"), TEST_INI_FILE).getPath();
	}

	protected ICustomIniFile getIniFile(String fileName) {
		if (mntmUseRegistry.getSelection())
			return new TRegistryIniFile(fileName);
		else
			return new TIniFile(fileName);
	}

	protected void loadRegistryAction() {
		try (ICustomIniFile ini = new TIniFile(iniFileName())) {
			mntmUseRegistry.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"UseRegistry", mntmUseRegistry.getSelection()));
		}
	}

	protected void saveRegistryAction() {
		if (mntmUseRegistry.getSelection())
			new File(iniFileName()).delete();

		try (ICustomIniFile ini = new TIniFile(iniFileName())) {
			ini.writeBool(CN_CONFIG_INI_SECTION, "UseRegistry", mntmUseRegistry.getSelection());
		}
	}


	protected void loadFormPlacement() {
		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			shell.setBounds(
					ini.readInteger(CN_CONFIG_INI_SECTION, "Left", shell.getLocation().x),
					ini.readInteger(CN_CONFIG_INI_SECTION, "Top", shell.getLocation().y),
					ini.readInteger(CN_CONFIG_INI_SECTION, "Width", shell.getSize().x),
					ini.readInteger(CN_CONFIG_INI_SECTION, "Height", shell.getSize().y)
			);
			if (ini.readBool(CN_CONFIG_INI_SECTION, "Maximized", false))
				shell.setMaximized(true);
		}
	}

	protected void saveFormPlacement() {
		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			ini.writeBool(CN_CONFIG_INI_SECTION, "AutoSave", mntmAutoSave.getSelection());

			if (!shell.getMaximized()) {
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Left", shell.getLocation().x);
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Top", shell.getLocation().y);
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Width", shell.getSize().x);
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Height", shell.getSize().y);
			}

			ini.writeBool(CN_CONFIG_INI_SECTION, "Maximized", shell.getMaximized());
		}

	}


	protected void saveConfiguration() {
		if (fSuite != null)
			saveSuiteConfiguration();

		saveFormPlacement();
		saveRegistryAction();

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
				// splitter locations
			int[] weights = sashForm.getWeights();
			for (int i = 0; i < weights.length; ++i) {
				ini.writeInteger(CN_CONFIG_INI_SECTION, "sashForm.weights[" + i + "]", weights[i]);
			}

			// error box
			ini.writeBool(CN_CONFIG_INI_SECTION, "ErrorMessage.Visible", mntmErrorBoxVisible.getSelection());

			// failure list configuration
			for (int i = 0; i < tableFailureList.getColumnCount(); ++i) {
				ini.writeInteger(CN_CONFIG_INI_SECTION, "FailureList.ColumnWidth[" + i + "]",
						tableFailureList.getColumn(i).getWidth());
			}

			// other options
			ini.writeBool(CN_CONFIG_INI_SECTION, "HideTestNodesOnOpen", mntmHideTestNodesOnOpen.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "BreakOnFailures", mntmBreakOnFailures.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "FailOnNoChecksExecuted", mntmFailTestCaseIfNoChecksExecuted.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "FailOnMemoryLeaked", mntmFailTestCaseIfMemoryLeaked.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "IgnoreSetUpTearDownLeaks", mntmIgnoreMemoryLeakInSetUpTearDown.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "ReportMemoryLeakTypes", mntmReportMemoryLeakTypeOnShutdown.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "SelectTestedNode", mntmShowTestedNode.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "WarnOnFailTestOverride", mntmWarnIfFailTestOverridden.getSelection());
			ini.writeInteger(CN_CONFIG_INI_SECTION, "PopupX", fPopupX);
			ini.writeInteger(CN_CONFIG_INI_SECTION, "PopupY", fPopupY);
		}
	}

	protected void loadConfiguration() {
		loadRegistryAction();
		loadFormPlacement();
		loadSuiteConfiguration();

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			mntmAutoSave.setSelection(ini.readBool(CN_CONFIG_INI_SECTION, "AutoSave", mntmAutoSave.getSelection()));

			// splitter locations
			int[] weights = sashForm.getWeights();
			for (int i = 0; i < weights.length; ++i) {
				weights[i] = ini.readInteger(CN_CONFIG_INI_SECTION, "sashForm.weights[" + i + "]", weights[i]);
			}
			sashForm.setWeights(weights);

			// error box
			mntmErrorBoxVisible.setSelection(ini.readBool(CN_CONFIG_INI_SECTION, "ErrorMessage.Visible",
					mntmErrorBoxVisible.getSelection()));

			compositeErrorBox.setVisible(mntmErrorBoxVisible.getSelection());
			shell.layout(true, true);

			// failure list configuration
			for (int i = 0; i < tableFailureList.getColumnCount(); ++i) {
				TableColumn column = tableFailureList.getColumn(i);
				column.setWidth(ini.readInteger(CN_CONFIG_INI_SECTION, "FailureList.ColumnWidth[" + i + "]", column.getWidth()));
			}

			// other options
			mntmHideTestNodesOnOpen.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"HideTestNodesOnOpen", mntmHideTestNodesOnOpen.getSelection()));
			mntmBreakOnFailures.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"BreakOnFailures", mntmBreakOnFailures.getSelection()));
			mntmFailTestCaseIfNoChecksExecuted.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"FailOnNoChecksExecuted", mntmFailTestCaseIfNoChecksExecuted.getSelection()));
			mntmFailTestCaseIfMemoryLeaked.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"FailOnMemoryLeaked", mntmFailTestCaseIfMemoryLeaked.getSelection()));
			mntmIgnoreMemoryLeakInSetUpTearDown.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"IgnoreSetUpTearDownLeaks", mntmIgnoreMemoryLeakInSetUpTearDown.getSelection()));
			mntmReportMemoryLeakTypeOnShutdown.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"ReportMemoryLeakTypes", mntmReportMemoryLeakTypeOnShutdown.getSelection()));
			mntmWarnIfFailTestOverridden.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"WarnOnFailTestOverride", mntmWarnIfFailTestOverridden.getSelection()));
			mntmShowTestedNode.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"SelectTestedNode", mntmShowTestedNode.getSelection()));
			fPopupX = ini.readInteger(CN_CONFIG_INI_SECTION, "PopupX", 350);
			fPopupY = ini.readInteger(CN_CONFIG_INI_SECTION, "PopupY", 30);
		}

		if (fSuite != null)
			updateTestTreeState();
	}

	private void saveSuiteConfiguration(ICustomIniFile ini, final String section, final TreeItem node) {
		String testShortDisplayName = getShortDisplayName(nodeToTest(node));
		if (node.getChecked())
			ini.deleteKey(section, testShortDisplayName);
		else
			ini.writeBool(section, testShortDisplayName, false);

		for(TreeItem childNode: node.getItems()) {
			saveSuiteConfiguration(ini, section + "." + testShortDisplayName, childNode);
		}
	}

	protected void saveSuiteConfiguration() {
		if (fSuite == null)
			return;

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			for (TreeItem node: testTree.getItems())
				saveSuiteConfiguration(ini, "Tests", node);
		}
	}

	private void loadSuiteConfiguration(ICustomIniFile ini, final String section, final TreeItem node) {
		Description test = nodeToTest(node);
		node.setChecked(ini.readBool(section, getShortDisplayName(test), true));

		for(TreeItem childNode: node.getItems()) {
			loadSuiteConfiguration(ini, section + "." + getShortDisplayName(test), childNode);
		}
	}

	protected void loadSuiteConfiguration() {
		if (fSuite == null)
			return;

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			for (TreeItem node: testTree.getItems())
				loadSuiteConfiguration(ini, "Tests", node);
		}
	}

	protected void autoSaveConfiguration() {
		if (mntmAutoSave.getSelection())
			saveConfiguration();
	}


	protected boolean nodeIsGrandparent(TreeItem node) {
		boolean result = false;
		for (TreeItem childNode: node.getItems()) {
			result = childNode.getItemCount() > 0 || result;
			collapseNonGrandparentNodes(childNode);
		}
		return result;
	}

	protected void collapseNonGrandparentNodes(TreeItem rootNode) {
		if (!nodeIsGrandparent(rootNode))
			rootNode.setExpanded(false);

		for (TreeItem childNode: rootNode.getItems()) {
			collapseNonGrandparentNodes(childNode);
		}
	}


	protected void clearStatusMessage() {
		// XXX == clearFailureMessage()
		errorMessageStyledText.setText("");
	}


	protected void setupCustomShortcuts() {
		// do nothing: shortcuts already changed in the GUI
	}

	protected void setupGuiNodes() {
		// do nothing: testToNodeMap already filled in fillTestTree()
	}


	private boolean selectNodeIfTestEnabled(TreeItem node) {
		Description test = nodeToTest(node);
		if (node.getChecked() && test.isTest()) {
			selectNode(node);
			return true;
		} else
			return false;
	}


	private class RunTheTestFilter extends Filter {

		@Override
		public boolean shouldRun(Description description) {
			if (fSelectedTests == null) {
				TreeItem node = testToNode(description); // XXX assert node != null;
				return node != null && node.getChecked();
			} else {
				return fSelectedTests.contains(description);
			}
		}

		@Override
		public String describe() {
			return getClass().getSimpleName();
		}

	}

	// TODO possible to use @RunListener.ThreadSafe
	private class RunTheTestListener extends RunListener {

		boolean lastTestFail = false;
		Deque<TreeItem> startedSuites = new ArrayDeque<>();
		boolean lastSuiteStoppedHalfway = false;

		private void startNewSuite(TreeItem node) {
			Description startedParentTest = nodeToTest(node);
			startTest(startedParentTest);
			startSuite(startedParentTest);
			startedSuites.push(node);
		}

		private void stopLastStartedSuite(boolean halfway) {
			assert !startedSuites.isEmpty();
			Description endedSuite = nodeToTest(halfway ? startedSuites.peek() : startedSuites.pop());
			if (!lastSuiteStoppedHalfway) {
				endSuite(endedSuite);
				if (!halfway)
					addSuccess(endedSuite);
			}
			if (!halfway)
				endTest(endedSuite);
			lastSuiteStoppedHalfway = halfway;
		}

		private void stopStartedSuites(TreeItem upToNode) {
			while(!startedSuites.isEmpty() && startedSuites.peek() != upToNode) {
				stopLastStartedSuite(false);
			}
		}

		private void updateStartedSuites(TreeItem suiteNode) {
			//if (suiteNode == startedSuites.peekLast()) return; // optimization
			Deque<TreeItem> newSuites = new ArrayDeque<>();
			while (suiteNode != null && !startedSuites.contains(suiteNode)) {
				newSuites.push(suiteNode);
				suiteNode = suiteNode.getParentItem();
			}
			stopStartedSuites(suiteNode);

			while (!newSuites.isEmpty())
				startNewSuite(newSuites.pop());
		}

		// XXX This may be called on an arbitrary thread.
		@Override
		public void testRunStarted(final Description description) throws Exception {
			testingStarts();
		}

		// XXX This may be called on an arbitrary thread.
		@Override
		public void testRunFinished(final Result result) throws Exception {
			stopStartedSuites(null);
			testingEnds(result);
		}

		@Override
		public void testStarted(final Description description) throws Exception {
			updateStartedSuites(testToNode(description).getParentItem());
			startTest(description);
			lastTestFail = false;
		}

		@Override
		public void testFinished(final Description description) throws Exception {
			if (!lastTestFail)
				addSuccess(description);
			endTest(description);
		}

		@Override
		public void testFailure(final Failure failure) throws Exception {
			if (failure.getDescription().equals(Description.TEST_MECHANISM)) {
				// may be called on an arbitrary thread.
				if (!display.isDisposed()) {
					display.syncExec(new Runnable() {
						@Override public void run() {
							if (!display.isDisposed()) {
								showError(failure.getDescription().getDisplayName(), failure.getException());
							}
						}
					});
				}
				return;
			}
			if (failure.getDescription().isSuite()) {
				updateStartedSuites(testToNode(failure.getDescription()));
				stopLastStartedSuite(true);
			}
			addError(failure);
			lastTestFail = true;
		}

		@Override
		public void testAssumptionFailure(final Failure failure) {
			if (failure.getDescription().isSuite()) {
				updateStartedSuites(testToNode(failure.getDescription()));
				stopLastStartedSuite(true);
			}
			addFailure(failure);
			lastTestFail = true;
		}

		@Override
		public void testIgnored(Description description) throws Exception {
			addIgnored(description);
		}

	}

	private void showError(String message, final Throwable exception) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		messageBox.setText(shell.getText());
		if (message == null)
			message = "";
		if (exception != null) {
			StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			message += (message.isEmpty() ? "" : "\r\n") + sw.toString();
		}
		messageBox.setMessage(message);
		messageBox.open();
	}

	@Override
	public void testingStarts() {
		fTotalTime = 0;
		updateStatus(true);
		scoreBar.setBackground(clOk);
		scoreBar.update();
	}

	@Override
	public void startTest(Description description) {
		assert fTestResult != null && fNotifier != null;
		assert description != null;
		fTestTimeMap.put(description, new TestTime());
		TreeItem node = testToNode(description);
		assert node != null;
		setTreeNodeImage(node, imgRunning);
		if (mntmShowTestedNode.getSelection()) {
			makeNodeVisible(node);
			testTree.update();
		}
		clearStatusMessage();
		updateStatus(false);
	}

	@Override
	public void addSuccess(Description description) {
		assert description != null;
		setTreeNodeImage(testToNode(description), imgRun);
	}

	@Override
	public void addError(Failure failure) {
		TableItem item = addFailureItem(failure);
		item.setImage(imgError);
		scoreBar.setBackground(clError);
		scoreBar.update();

		setTreeNodeImage(testToNode(failure.getDescription()), imgError);
		updateStatus(false);

		if (mntmBreakOnFailures.getSelection() && fTestResult != null && fNotifier != null) {
			fNotifier.pleaseStop();
			selectNode(testToNode(failure.getDescription()));
		}
	}

	@Override
	public void addFailure(Failure failure) {
		TableItem item = addFailureItem(failure);
		item.setImage(imgFailed);
		if (!clError.equals(scoreBar.getBackground())) {
			scoreBar.setBackground(clFailure);
			scoreBar.update();
		}
		setTreeNodeImage(testToNode(failure.getDescription()), imgFailed);
		fAssumptionFailureCount++;
		updateStatus(false);
	}

	@Override
	public void endTest(Description description) {
		if (fTestTimeMap.containsKey(description))
			fTestTimeMap.get(description).stop();
		updateStatus(false);
	}

	@Override
	public void testingEnds(Result result) {
		fTotalTime = result.getRunTime();
	}

	@Override
	public void startSuite(Description description) {

	}

	@Override
	public void endSuite(Description description) {
		updateStatus(true);
	}

	public void addIgnored(Description description) {
		testToNode(description).setImage(imgHasProps);
		updateStatus(false);
	}

	private long getElapsedTestTime(Description test) {
		if (fTestTimeMap.containsKey(test))
			return fTestTimeMap.get(test).getElapsed();
		return 0;
	}

	private int countEnabledTestCases(TreeItem treeItem) {
		int result = 0;
		if (treeItem.getChecked()) {
			for (TreeItem childItem: treeItem.getItems()) {
				result += countEnabledTestCases(childItem);
			}
			if ((fTests.get((int) treeItem.getData())).isTest())
				result++;
		}
		return result;
	}

	private int countEnabledTestCases() {
		int result = 0;
		for (TreeItem treeItem: testTree.getItems()) {
			result += countEnabledTestCases(treeItem);
		}
		return result;
	}


	public int getFailureCount() {
		return fFailureCount;
	}

	public int getIgnoreCount() {
		return fIgnoreCount;
	}

	private String getShortDisplayName(Description test) {
		String result = test.getDisplayName();
		if (test.isTest()) {
			String suffix = "(" + test.getClassName() + ")";
			if (test.getDisplayName().endsWith(suffix))
				result = result.substring(0, result.length() - suffix.length());
		}
		if (test.isSuite()) {
			int pos = Math.max(result.lastIndexOf('.'), result.lastIndexOf('$'));
			if (pos >= 0)
				result = result.substring(pos + 1);
		}
		return result;
	}
}
