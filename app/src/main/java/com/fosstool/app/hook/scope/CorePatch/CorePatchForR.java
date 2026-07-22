package com.fosstool.app.hook.scope.CorePatch;

import static com.fosstool.app.utils.SPUtilsKt.ModulePrefs;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;

import com.fosstool.app.BuildConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import android.app.AndroidAppHelper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("ALL")
public class CorePatchForR extends XposedHelper implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = "CorePatch";
    final XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, ModulePrefs);

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        ClassLoader classLoader = loadPackageParam.classLoader;

        // === PackageManagerService ===
        hookPackageManagerService(classLoader);

        // === PackageManagerServiceUtils ===
        hookPackageManagerServiceUtils(classLoader);

        // === ApkSignatureVerifier ===
        hookApkSignatureVerifier(classLoader);

        // === ApkSigningBlockUtils ===
        hookApkSigningBlockUtils(classLoader);

        // === StrictJarVerifier ===
        hookStrictJarVerifier(classLoader);

        // === MessageDigest ===
        hookMessageDigest(classLoader);

        // === AssetManager ===
        hookAssetManager(classLoader);

        // === SigningDetails ===
        hookSigningDetails(classLoader);

        // === ApplicationInfo ===
        hookApplicationInfo(classLoader);

        // === KeySetManagerService ===
        hookKeySetManagerService(classLoader);

        // === NtConfigListServiceImpl (bypass block for Nothing Phone) ===
        hookNtConfigListServiceImpl(classLoader);

        // === SharedUserSetting (bypass shared user) ===
        hookSharedUserSetting(classLoader);

        // === ReconcilePackageUtils ===
        hookReconcilePackageUtils(classLoader);

        // === VerificationParams (SDK33) / VerifyingSession (SDK34+) ===
        hookVerificationAgent(classLoader);

        // === InstallPackageHelper (SDK33+) ===
        hookInstallPackageHelper(classLoader);

        // === ScanPackageUtils (SDK33+) ===
        hookScanPackageUtils(classLoader);
    }

    private void hookPackageManagerService(ClassLoader classLoader) {
        Class<?> pmsClass = findClass("com.android.server.pm.PackageManagerService", classLoader);
        if (pmsClass == null) return;

        // checkDowngrade - allow downgrade install
        // SDK 30-32: in PackageManagerService, SDK 33+: in PackageManagerServiceUtils
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            try {
                Method checkDowngrade = Arrays.stream(pmsClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals("checkDowngrade") && m.getReturnType() == Void.TYPE)
                        .findFirst().orElse(null);
                if (checkDowngrade != null) {
                    XposedBridge.hookMethod(checkDowngrade, new ReturnConstant(prefs, "downgrade", null, true));
                }
            } catch (Throwable e) {
                if (DEBUG) Log.e(TAG, "CorePatch: checkDowngrade hook failed", e);
            }
        }

        // isVerificationEnabled - disable verification agent
        try {
            Method isVerificationEnabled = Arrays.stream(pmsClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("isVerificationEnabled") && m.getReturnType() == Boolean.TYPE)
                    .findFirst().orElse(null);
            if (isVerificationEnabled != null) {
                XposedBridge.hookMethod(isVerificationEnabled, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("disableVerificationAgent", true)) {
                            param.setResult(false);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: isVerificationEnabled hook failed", e);
        }

        // doesSignatureMatchForPermissions (SDK 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Method doesSigMatch = Arrays.stream(pmsClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals("doesSignatureMatchForPermissions"))
                        .findFirst().orElse(null);
                if (doesSigMatch != null) {
                    XposedBridge.hookMethod(doesSigMatch, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            if (prefs.getBoolean("digestCreak", true) && prefs.getBoolean("UsePreSig", false)) {
                                if (param.getResult().equals(false)) {
                                    String pPname = (String) XposedHelpers.callMethod(param.args[1], "getPackageName");
                                    if (pPname.contentEquals((String) param.args[0])) {
                                        param.setResult(true);
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (Throwable e) {
                if (DEBUG) Log.e(TAG, "CorePatch: doesSignatureMatchForPermissions hook failed", e);
            }
        }
    }

    private void hookPackageManagerServiceUtils(ClassLoader classLoader) {
        Class<?> utilsClass = findClass("com.android.server.pm.PackageManagerServiceUtils", classLoader);
        if (utilsClass == null) return;

        // verifySignatures - deoptimize and bypass
        try {
            Method verifySignatures = Arrays.stream(utilsClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("verifySignatures") && m.getReturnType() == Boolean.TYPE)
                    .findFirst().orElse(null);
            if (verifySignatures != null) {
                deoptimizeMethod(verifySignatures);
                XposedBridge.hookMethod(verifySignatures, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("authcreak", false)) {
                            param.setResult(false);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: verifySignatures hook failed", e);
        }

        // checkDowngrade (SDK 33+ - moved from PMS to Utils)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                Arrays.stream(utilsClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals("checkDowngrade") && m.getReturnType() == Void.TYPE)
                        .filter(m -> m.getParameterTypes().length == 2
                                && m.getParameterTypes()[1].getName().equals("android.content.pm.PackageInfoLite"))
                        .forEach(m -> {
                            try {
                                XposedBridge.hookMethod(m, new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) {
                                        prefs.reload();
                                        if (prefs.getBoolean("downgrade", true)) {
                                            param.setResult(null);
                                        }
                                    }
                                });
                            } catch (Throwable e) {
                                if (DEBUG) Log.e(TAG, "CorePatch: checkDowngrade(Utils) hook failed", e);
                            }
                        });
            } catch (Throwable e) {
                if (DEBUG) Log.e(TAG, "CorePatch: checkDowngrade Utils hook failed", e);
            }
        }

        // canJoinSharedUserId (SDK 33+) - deoptimize to ensure verifySignatures success
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                Method canJoinSharedUserId = Arrays.stream(utilsClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals("canJoinSharedUserId"))
                        .findFirst().orElse(null);
                if (canJoinSharedUserId != null) {
                    deoptimizeMethod(canJoinSharedUserId);
                }
            } catch (Throwable e) {
                if (DEBUG) Log.e(TAG, "CorePatch: canJoinSharedUserId deoptimize failed", e);
            }
        }
    }

    private void hookApkSignatureVerifier(ClassLoader classLoader) {
        Class<?> asvClass = findClass("android.util.apk.ApkSignatureVerifier", classLoader);
        if (asvClass == null) return;

        // getMinimumSignatureSchemeVersionForTargetSdk (SDK 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            findAndHookMethod("android.util.apk.ApkSignatureVerifier", classLoader,
                    "getMinimumSignatureSchemeVersionForTargetSdk", int.class,
                    new ReturnConstant(prefs, "authcreak", 0, false));
        }

        // verifyV1Signature (SDK <= 32)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            hookVerifyV1SignaturePreT(asvClass, classLoader);
        }
        // verifyV1Signature (SDK 33+)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hookVerifyV1SignatureTPlus(asvClass, classLoader);
        }
    }

    @SuppressWarnings("unchecked")
    private void hookVerifyV1SignaturePreT(Class<?> asvClass, ClassLoader classLoader) {
        try {
            Class<?> signingDetailsClazz = getSigningDetails(classLoader);
            Constructor<?> signingDetailsConstructor = signingDetailsClazz.getDeclaredConstructor(Signature[].class, int.class);
            signingDetailsConstructor.setAccessible(true);

            Class<?> packageParserExceptionClazz = findClass("android.content.pm.PackageParser$PackageParserException", classLoader);
            final Field errorField;
            if (packageParserExceptionClazz != null) {
                errorField = packageParserExceptionClazz.getDeclaredField("error");
                errorField.setAccessible(true);
            } else {
                errorField = null;
            }

            Class<?> strictJarFileClazz = findClass("android.util.jar.StrictJarFile", classLoader);
            final Constructor<?> strictJarFileConstructor;
            if (strictJarFileClazz != null) {
                strictJarFileConstructor = strictJarFileClazz.getDeclaredConstructor(String.class, boolean.class, boolean.class);
                strictJarFileConstructor.setAccessible(true);
            } else {
                strictJarFileConstructor = null;
            }

            Method verifyV1 = Arrays.stream(asvClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("verifyV1Signature"))
                    .findFirst().orElse(null);
            if (verifyV1 == null) return;

            XposedBridge.hookMethod(verifyV1, new XC_MethodHook() {
                @Override
                public void afterHookedMethod(MethodHookParam methodHookParam) {
                    prefs.reload();
                    if (!prefs.getBoolean("authcreak", false)) return;

                    Throwable throwable = methodHookParam.getThrowable();
                    if (throwable == null) return;

                    Signature[] lastSigs = null;
                    if (prefs.getBoolean("UsePreSig", false)) {
                        try {
                            PackageManager PM = AndroidAppHelper.currentApplication().getPackageManager();
                            if (PM != null) {
                                PackageInfo pI = PM.getPackageArchiveInfo((String) methodHookParam.args[0], 0);
                                if (pI != null) {
                                    PackageInfo InstpI = PM.getPackageInfo(pI.packageName, PackageManager.GET_SIGNATURES);
                                    lastSigs = InstpI.signatures;
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    if (lastSigs == null && prefs.getBoolean("digestCreak", true) && strictJarFileConstructor != null) {
                        try {
                            Object origJarFile = strictJarFileConstructor.newInstance(methodHookParam.args[0], true, false);
                            ZipEntry manifestEntry = (ZipEntry) XposedHelpers.callMethod(origJarFile, "findEntry", "AndroidManifest.xml");
                            Certificate[][] lastCerts = (Certificate[][]) XposedHelpers.callStaticMethod(asvClass, "loadCertificates", origJarFile, manifestEntry);
                            lastSigs = (Signature[]) XposedHelpers.callStaticMethod(asvClass, "convertToSignatures", (Object) lastCerts);
                        } catch (Throwable ignored) {
                        }
                    }

                    Object[] signingDetailsArgs = new Object[2];
                    signingDetailsArgs[0] = lastSigs != null ? lastSigs : new Signature[]{new Signature(SIGNATURE)};
                    signingDetailsArgs[1] = 1;
                    Object newInstance;
                    try {
                        newInstance = signingDetailsConstructor.newInstance(signingDetailsArgs);
                    } catch (Throwable ignored) {
                        return;
                    }

                    if (packageParserExceptionClazz != null && errorField != null) {
                        try {
                            if (throwable.getClass() == packageParserExceptionClazz) {
                                if (errorField.getInt(throwable) == -103) {
                                    methodHookParam.setResult(newInstance);
                                }
                            }
                            Throwable cause = throwable.getCause();
                            if (cause != null && cause.getClass() == packageParserExceptionClazz) {
                                if (errorField.getInt(cause) == -103) {
                                    methodHookParam.setResult(newInstance);
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: verifyV1Signature pre-T hook failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void hookVerifyV1SignatureTPlus(Class<?> asvClass, ClassLoader classLoader) {
        try {
            Class<?> signingDetailsClazz = getSigningDetails(classLoader);
            final Constructor<?> signingDetailsConstructor = signingDetailsClazz.getDeclaredConstructor(Signature[].class, int.class);
            signingDetailsConstructor.setAccessible(true);

            Class<?> packageParserExceptionClazz = findClass("android.content.pm.PackageParser$PackageParserException", classLoader);
            final Field errorField;
            if (packageParserExceptionClazz != null) {
                errorField = packageParserExceptionClazz.getDeclaredField("error");
                errorField.setAccessible(true);
            } else {
                errorField = null;
            }

            Class<?> strictJarFileClazz = findClass("android.util.jar.StrictJarFile", classLoader);
            final Constructor<?> strictJarFileConstructor;
            if (strictJarFileClazz != null) {
                strictJarFileConstructor = strictJarFileClazz.getDeclaredConstructor(String.class, boolean.class, boolean.class);
                strictJarFileConstructor.setAccessible(true);
            } else {
                strictJarFileConstructor = null;
            }

            Class<?> signingDetailsWithDigestsClazz = findClass("android.util.apk.ApkSignatureVerifier$SigningDetailsWithDigests", classLoader);
            final Constructor<?> signingDetailsWithDigestsConstructor;
            if (signingDetailsWithDigestsClazz != null) {
                signingDetailsWithDigestsConstructor = signingDetailsWithDigestsClazz.getDeclaredConstructor(signingDetailsClazz, Map.class);
                signingDetailsWithDigestsConstructor.setAccessible(true);
            } else {
                signingDetailsWithDigestsConstructor = null;
            }

            Method verifyV1 = Arrays.stream(asvClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("verifyV1Signature"))
                    .findFirst().orElse(null);
            if (verifyV1 == null) return;

            XposedBridge.hookMethod(verifyV1, new XC_MethodHook() {
                @Override
                public void afterHookedMethod(MethodHookParam methodHookParam) {
                    prefs.reload();
                    if (!prefs.getBoolean("authcreak", false)) return;

                    Integer parseErr = null;
                    Object result = methodHookParam.getResult();
                    Class<?> parseResultClass = findClass("android.content.pm.parsing.result.ParseResult", classLoader);
                    if (parseResultClass != null && verifyV1.getReturnType() == parseResultClass) {
                        if ((boolean) XposedHelpers.callMethod(result, "isError")) {
                            parseErr = (int) XposedHelpers.callMethod(result, "getErrorCode");
                        }
                    }

                    Throwable throwable = methodHookParam.getThrowable();
                    if (throwable == null && parseErr == null) return;

                    Signature[] lastSigs = null;
                    if (prefs.getBoolean("UsePreSig", false)) {
                        try {
                            PackageManager PM = AndroidAppHelper.currentApplication().getPackageManager();
                            if (PM != null) {
                                PackageInfo pI = PM.getPackageArchiveInfo((String) methodHookParam.args[1], 0);
                                if (pI != null) {
                                    PackageInfo InstpI = PM.getPackageInfo(pI.packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                                    Object signingInfo = InstpI.signingInfo;
                                    if (signingInfo != null) {
                                        lastSigs = (Signature[]) XposedHelpers.callMethod(signingInfo, "getSigningCertificateHistory");
                                    }
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    if (lastSigs == null && prefs.getBoolean("digestCreak", true) && strictJarFileConstructor != null) {
                        try {
                            Object origJarFile = strictJarFileConstructor.newInstance(methodHookParam.args[1], true, false);
                            ZipEntry manifestEntry = (ZipEntry) XposedHelpers.callMethod(origJarFile, "findEntry", "AndroidManifest.xml");
                            Method loadCerts = Arrays.stream(asvClass.getDeclaredMethods())
                                    .filter(m -> m.getName().equals("loadCertificates")).findFirst().orElse(null);
                            if (loadCerts != null) {
                                loadCerts.setAccessible(true);
                                Object certs;
                                if (loadCerts.getParameterTypes().length == 3) {
                                    certs = loadCerts.invoke(null, methodHookParam.args[0], origJarFile, manifestEntry, false);
                                } else {
                                    certs = loadCerts.invoke(null, origJarFile, manifestEntry);
                                }
                                if (parseResultClass != null && certs != null && certs.getClass() == parseResultClass) {
                                    certs = XposedHelpers.callMethod(certs, "getResult");
                                }
                                Method convertToSignatures = Arrays.stream(asvClass.getDeclaredMethods())
                                        .filter(m -> m.getName().equals("convertToSignatures")).findFirst().orElse(null);
                                if (convertToSignatures != null) {
                                    convertToSignatures.setAccessible(true);
                                    lastSigs = (Signature[]) convertToSignatures.invoke(null, (Object) certs);
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    Object[] signingDetailsArgs = new Object[2];
                    signingDetailsArgs[0] = lastSigs != null ? lastSigs : new Signature[]{new Signature(SIGNATURE)};
                    signingDetailsArgs[1] = 1;
                    Object signingDetails;
                    Object newResult;
                    try {
                        signingDetails = signingDetailsConstructor.newInstance(signingDetailsArgs);
                        newResult = signingDetails;
                        if (signingDetailsWithDigestsConstructor != null) {
                            newResult = signingDetailsWithDigestsConstructor.newInstance(signingDetails, null);
                        }
                    } catch (Throwable ignored) {
                        return;
                    }

                    if (throwable != null && packageParserExceptionClazz != null && errorField != null) {
                        try {
                            if (throwable.getClass() == packageParserExceptionClazz && errorField.getInt(throwable) == -103) {
                                methodHookParam.setResult(newResult);
                                methodHookParam.setThrowable(null);
                            }
                            Throwable cause = throwable.getCause();
                            if (cause != null && cause.getClass() == packageParserExceptionClazz && errorField.getInt(cause) == -103) {
                                methodHookParam.setResult(newResult);
                                methodHookParam.setThrowable(null);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    if (parseErr != null && parseErr == -103) {
                        Object input = methodHookParam.args[0];
                        XposedHelpers.callMethod(input, "reset");
                        methodHookParam.setResult(XposedHelpers.callMethod(input, "success", newResult));
                    }
                }
            });
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: verifyV1Signature T+ hook failed", e);
        }
    }

    private void hookApkSigningBlockUtils(ClassLoader classLoader) {
        Class<?> asbuClass = findClass("android.util.apk.ApkSigningBlockUtils", classLoader);
        if (asbuClass == null) return;

        // parseVerityDigestAndVerifySourceLength
        try {
            Method parseVerity = Arrays.stream(asbuClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("parseVerityDigestAndVerifySourceLength"))
                    .findFirst().orElse(null);
            if (parseVerity != null) {
                XposedBridge.hookMethod(parseVerity, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("authcreak", false)) {
                            param.setResult(((byte[]) param.args[0]).length > 32
                                    ? Arrays.copyOfRange((byte[]) param.args[0], 0, 32)
                                    : param.args[0]);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: parseVerityDigestAndVerifySourceLength hook failed", e);
        }

        // verifyIntegrityForVerityBasedAlgorithm
        try {
            Method verifyIntegrity = Arrays.stream(asbuClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("verifyIntegrityForVerityBasedAlgorithm"))
                    .findFirst().orElse(null);
            if (verifyIntegrity != null) {
                XposedBridge.hookMethod(verifyIntegrity, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("authcreak", false)) {
                            param.setResult(null);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: verifyIntegrityForVerityBasedAlgorithm hook failed", e);
        }
    }

    private void hookStrictJarVerifier(ClassLoader classLoader) {
        Class<?> sjvClass = findClass("android.util.jar.StrictJarVerifier", classLoader);
        if (sjvClass == null) return;

        // verifyMessageDigest
        hookAllMethods("android.util.jar.StrictJarVerifier", classLoader, "verifyMessageDigest",
                new ReturnConstant(prefs, "authcreak", true, false));

        // verify
        try {
            Arrays.stream(sjvClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("verify") && m.getReturnType() == Boolean.TYPE)
                    .forEach(m -> {
                        try {
                            XposedBridge.hookMethod(m, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    prefs.reload();
                                    if (prefs.getBoolean("authcreak", false)) {
                                        param.setResult(true);
                                    }
                                }
                            });
                        } catch (Throwable ignored) {
                        }
                    });
        } catch (Throwable ignored) {
        }

        // constructor - signatureSchemeRollbackProtectionsEnforced
        try {
            Field rollbackField = sjvClass.getDeclaredField("signatureSchemeRollbackProtectionsEnforced");
            rollbackField.setAccessible(true);
            XposedBridge.hookAllConstructors(sjvClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    prefs.reload();
                    if (prefs.getBoolean("authcreak", false)) {
                        try {
                            rollbackField.set(param.thisObject, false);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        // verifyBytes - extract real signature when digest creak enabled
        try {
            Class<?> pkcs7Class = findClass("sun.security.pkcs.PKCS7", classLoader);
            Class<?> signerInfoClass = findClass("sun.security.pkcs.SignerInfo", classLoader);
            if (pkcs7Class != null && signerInfoClass != null) {
                Constructor<?> pkcs7Constructor = pkcs7Class.getDeclaredConstructor(byte[].class);
                pkcs7Constructor.setAccessible(true);
                Method getSignerInfos = pkcs7Class.getDeclaredMethod("getSignerInfos");
                getSignerInfos.setAccessible(true);
                Method getCertificateChain = signerInfoClass.getDeclaredMethod("getCertificateChain", pkcs7Class);
                getCertificateChain.setAccessible(true);

                Method verifyBytes = sjvClass.getDeclaredMethod("verifyBytes", byte[].class, byte[].class);
                verifyBytes.setAccessible(true);

                XposedBridge.hookMethod(verifyBytes, new XC_MethodHook() {
                    @Override
                    public void afterHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("digestCreak", true) && !prefs.getBoolean("UsePreSig", false)) {
                            try {
                                Object block = pkcs7Constructor.newInstance(param.args[0]);
                                Object[] signerInfos = (Object[]) getSignerInfos.invoke(block);
                                if (signerInfos != null && signerInfos.length > 0) {
                                    Object signer = signerInfos[0];
                                    List<X509Certificate> certs = (List<X509Certificate>) getCertificateChain.invoke(signer, block);
                                    if (certs != null) {
                                        param.setResult(certs.toArray(new X509Certificate[0]));
                                    }
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: verifyBytes hook failed", e);
        }
    }

    private void hookMessageDigest(ClassLoader classLoader) {
        hookAllMethods("java.security.MessageDigest", classLoader, "isEqual",
                new ReturnConstant(prefs, "authcreak", true, false));
    }

    private void hookAssetManager(ClassLoader classLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return;
        findAndHookMethod("android.content.res.AssetManager", classLoader,
                "containsAllocatedTable", new ReturnConstant(prefs, "authcreak", false, false));
    }

    private void hookSigningDetails(ClassLoader classLoader) {
        Class<?> signingDetailsClazz = getSigningDetails(classLoader);
        if (signingDetailsClazz == null) return;

        // checkCapability
        try {
            Method checkCapability = Arrays.stream(signingDetailsClazz.getDeclaredMethods())
                    .filter(m -> m.getName().equals("checkCapability"))
                    .filter(m -> m.getParameterTypes().length == 2 && m.getParameterTypes()[1] == int.class)
                    .findFirst().orElse(null);
            if (checkCapability != null) {
                XposedBridge.hookMethod(checkCapability, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("digestCreak", true)) {
                            int flags = (Integer) param.args[1];
                            if (flags != 4 && flags != 16) {
                                param.setResult(true);
                            }
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: checkCapability hook failed", e);
        }

        // checkCapabilityRecover
        try {
            Method checkCapabilityRecover = Arrays.stream(signingDetailsClazz.getDeclaredMethods())
                    .filter(m -> m.getName().equals("checkCapabilityRecover"))
                    .filter(m -> m.getParameterTypes().length == 2 && m.getParameterTypes()[1] == int.class)
                    .findFirst().orElse(null);
            if (checkCapabilityRecover != null) {
                XposedBridge.hookMethod(checkCapabilityRecover, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("digestCreak", true)) {
                            int flags = (Integer) param.args[1];
                            if (flags != 4 && flags != 16) {
                                param.setResult(true);
                            }
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: checkCapabilityRecover hook failed", e);
        }

        // hasCommonAncestor (SDK 30+) - for shared user
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Method hasCommonAncestor = Arrays.stream(signingDetailsClazz.getDeclaredMethods())
                        .filter(m -> m.getName().equals("hasCommonAncestor"))
                        .filter(m -> m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == signingDetailsClazz)
                        .findFirst().orElse(null);
                if (hasCommonAncestor != null) {
                    XposedBridge.hookMethod(hasCommonAncestor, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            prefs.reload();
                            if (prefs.getBoolean("digestCreak", true) && prefs.getBoolean("sharedUser", false)) {
                                if (Arrays.stream(Thread.currentThread().getStackTrace())
                                        .anyMatch(o -> "verifySignatures".equals(o.getMethodName()))) {
                                    param.setResult(true);
                                }
                            }
                        }
                    });
                }
            } catch (Throwable e) {
                if (DEBUG) Log.e(TAG, "CorePatch: hasCommonAncestor hook failed", e);
            }
        }

        // signaturesMatchExactly - exact sig check
        try {
            Method signaturesMatchExactly = Arrays.stream(signingDetailsClazz.getDeclaredMethods())
                    .filter(m -> m.getName().equals("signaturesMatchExactly"))
                    .findFirst().orElse(null);
            if (signaturesMatchExactly != null) {
                XposedBridge.hookMethod(signaturesMatchExactly, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("exactSigCheck", false)) {
                            param.setResult(true);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: signaturesMatchExactly hook failed", e);
        }
    }

    private void hookApplicationInfo(ClassLoader classLoader) {
        findAndHookMethod("android.content.pm.ApplicationInfo", classLoader,
                "isPackageWhitelistedForHiddenApis", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("digestCreak", true)) {
                            ApplicationInfo info = (ApplicationInfo) param.thisObject;
                            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                                    || (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                                param.setResult(true);
                            }
                        }
                    }
                });
    }

    private void hookKeySetManagerService(ClassLoader classLoader) {
        Class<?> keySetManagerClass = findClass("com.android.server.pm.KeySetManagerService", classLoader);
        if (keySetManagerClass == null) return;

        ThreadLocal<Boolean> shouldBypass = new ThreadLocal<>();

        try {
            Method shouldCheck = Arrays.stream(keySetManagerClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("shouldCheckUpgradeKeySetLocked") && m.getReturnType() == Boolean.TYPE)
                    .findFirst().orElse(null);
            if (shouldCheck != null) {
                XposedBridge.hookMethod(shouldCheck, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("digestCreak", true) && Arrays.stream(Thread.currentThread().getStackTrace())
                                .anyMatch(o -> "preparePackage".equals(o.getMethodName())
                                        || "preparePackageLI".equals(o.getMethodName())
                                        || "installPackageLI".equals(o.getMethodName()))) {
                            shouldBypass.set(true);
                            param.setResult(true);
                        } else {
                            shouldBypass.set(false);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: shouldCheckUpgradeKeySetLocked hook failed", e);
        }

        try {
            Method checkUpgrade = Arrays.stream(keySetManagerClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("checkUpgradeKeySetLocked") && m.getReturnType() == Boolean.TYPE)
                    .findFirst().orElse(null);
            if (checkUpgrade != null) {
                XposedBridge.hookMethod(checkUpgrade, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("digestCreak", true) && shouldBypass.get() != null && shouldBypass.get()) {
                            param.setResult(true);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: checkUpgradeKeySetLocked hook failed", e);
        }
    }

    private void hookNtConfigListServiceImpl(ClassLoader classLoader) {
        Class<?> ntClass = findClass("com.nothing.server.ex.NtConfigListServiceImpl", classLoader);
        if (ntClass == null) return;

        try {
            Arrays.stream(ntClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("isInstallingAppForbidden") || m.getName().equals("isStartingAppForbidden"))
                    .forEach(m -> {
                        try {
                            XposedBridge.hookMethod(m, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    prefs.reload();
                                    if (prefs.getBoolean("bypassBlock", true)) {
                                        param.setResult(false);
                                    }
                                }
                            });
                        } catch (Throwable ignored) {
                        }
                    });
        } catch (Throwable ignored) {
        }
    }

    private void hookSharedUserSetting(ClassLoader classLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return;
        Class<?> sharedUserSettingClass = findClass("com.android.server.pm.SharedUserSetting", classLoader);
        if (sharedUserSettingClass == null) return;

        try {
            Field uidFlagsField = sharedUserSettingClass.getDeclaredField("uidFlags");
            uidFlagsField.setAccessible(true);

            Field packagesField = Arrays.stream(sharedUserSettingClass.getDeclaredFields())
                    .filter(f -> f.getName().equals("packages") || f.getName().equals("mPackages"))
                    .findFirst().orElse(null);
            if (packagesField == null) return;
            packagesField.setAccessible(true);

            Class<?> packageSignaturesClass = findClass("com.android.server.pm.PackageSignatures", classLoader);
            if (packageSignaturesClass == null) return;
            Field signingDetailsField = packageSignaturesClass.getDeclaredField("mSigningDetails");
            signingDetailsField.setAccessible(true);
            Class<?> signingDetailsClazz = signingDetailsField.getType();

            Method checkCapabilityMethod = signingDetailsClazz.getDeclaredMethod("checkCapability", signingDetailsClazz, int.class);
            checkCapabilityMethod.setAccessible(true);

            Method mergeLineageWithMethod;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mergeLineageWithMethod = signingDetailsClazz.getDeclaredMethod("mergeLineageWith", signingDetailsClazz, int.class);
            } else {
                mergeLineageWithMethod = signingDetailsClazz.getDeclaredMethod("mergeLineageWith", signingDetailsClazz);
            }
            mergeLineageWithMethod.setAccessible(true);

            final Method finalMergeLineageWithMethod = mergeLineageWithMethod;

            // removePackage hook
            Method removePackageMethod = Arrays.stream(sharedUserSettingClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("removePackage")).findFirst().orElse(null);
            if (removePackageMethod != null) {
                XposedBridge.hookMethod(removePackageMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (!prefs.getBoolean("digestCreak", true) || !prefs.getBoolean("sharedUser", false)) return;
                        try {
                            Object thisObject = param.thisObject;
                            int uidFlags = (int) uidFlagsField.get(thisObject);
                            if ((uidFlags & ApplicationInfo.FLAG_SYSTEM) != 0) return;
                            Object toRemove = param.args[0];
                            Object sharedUserSig = getSigningDetailsFromObject(thisObject);
                            if (sharedUserSig == null) return;
                            Object newSignatures = null;
                            boolean removed = false;
                            Object packagesSettings = getPackageStorage(packagesField.get(thisObject));
                            if (packagesSettings == null) return;
                            int pkgSize = (int) packagesSettings.getClass().getDeclaredMethod("size").invoke(packagesSettings);
                            if (pkgSize == 0) return;
                            Method valueAtMethod = packagesSettings.getClass().getDeclaredMethod("valueAt", int.class);
                            for (int i = 0; i < pkgSize; i++) {
                                Object pkg = valueAtMethod.invoke(packagesSettings, i);
                                if (pkg == null) continue;
                                if (pkg == toRemove) { removed = true; continue; }
                                Object pkgSig = getSigningDetailsFromObject(pkg);
                                if (pkgSig == null) continue;
                                boolean b1 = (boolean) checkCapabilityMethod.invoke(pkgSig, sharedUserSig, 0);
                                boolean b2 = (boolean) checkCapabilityMethod.invoke(sharedUserSig, pkgSig, 0);
                                if (b1 || b2) return;
                                newSignatures = newSignatures == null ? pkgSig
                                        : (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                        ? finalMergeLineageWithMethod.invoke(newSignatures, pkgSig, 2)
                                        : finalMergeLineageWithMethod.invoke(newSignatures, pkgSig));
                            }
                            if (!removed || newSignatures == null) return;
                            setSigningDetailsOnObject(thisObject, newSignatures);
                        } catch (Throwable ignored) {
                        }
                    }
                });
            }

            // addPackage hook
            Method addPackageMethod = Arrays.stream(sharedUserSettingClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("addPackage")).findFirst().orElse(null);
            if (addPackageMethod != null) {
                XposedBridge.hookMethod(addPackageMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (!prefs.getBoolean("digestCreak", true) || !prefs.getBoolean("sharedUser", false)) return;
                        try {
                            Object thisObject = param.thisObject;
                            int uidFlags = (int) uidFlagsField.get(thisObject);
                            if ((uidFlags & ApplicationInfo.FLAG_SYSTEM) != 0) return;
                            Object toAdd = param.args[0];
                            Object sharedUserSig = getSigningDetailsFromObject(thisObject);
                            if (sharedUserSig == null) return;
                            Object newSignatures = null;
                            boolean added = false;
                            Object packagesSettings = getPackageStorage(packagesField.get(thisObject));
                            if (packagesSettings == null) return;
                            int pkgSize = (int) packagesSettings.getClass().getDeclaredMethod("size").invoke(packagesSettings);
                            if (pkgSize == 0) return;
                            Method valueAtMethod = packagesSettings.getClass().getDeclaredMethod("valueAt", int.class);
                            for (int i = 0; i < pkgSize; i++) {
                                Object pkg = valueAtMethod.invoke(packagesSettings, i);
                                if (pkg == null) continue;
                                if (pkg == toAdd) { added = true; pkg = toAdd; }
                                Object pkgSig = getSigningDetailsFromObject(pkg);
                                if (pkgSig == null) continue;
                                boolean b1 = (boolean) checkCapabilityMethod.invoke(pkgSig, sharedUserSig, 0);
                                boolean b2 = (boolean) checkCapabilityMethod.invoke(sharedUserSig, pkgSig, 0);
                                if (b1 || b2) return;
                                newSignatures = newSignatures == null ? pkgSig
                                        : (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                        ? finalMergeLineageWithMethod.invoke(newSignatures, pkgSig, 2)
                                        : finalMergeLineageWithMethod.invoke(newSignatures, pkgSig));
                            }
                            if (!added || newSignatures == null) return;
                            setSigningDetailsOnObject(thisObject, newSignatures);
                        } catch (Throwable ignored) {
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: SharedUserSetting hook failed", e);
        }
    }

    private Object getPackageStorage(Object packagesSettings) {
        if (packagesSettings == null) return null;
        try {
            Field storageField = packagesSettings.getClass().getDeclaredField("mStorage");
            storageField.setAccessible(true);
            return storageField.get(packagesSettings);
        } catch (NoSuchFieldException e) {
            return packagesSettings;
        } catch (Throwable e) {
            return null;
        }
    }

    private Object getSigningDetailsFromObject(Object obj) {
        if (obj == null) return null;
        try {
            Field signaturesField;
            try {
                signaturesField = obj.getClass().getDeclaredField("signatures");
            } catch (NoSuchFieldException e) {
                signaturesField = obj.getClass().getSuperclass().getDeclaredField("signatures");
            }
            signaturesField.setAccessible(true);
            Object signatures = signaturesField.get(obj);
            if (signatures == null) return null;
            Field mSigningDetailsField = signatures.getClass().getDeclaredField("mSigningDetails");
            mSigningDetailsField.setAccessible(true);
            return mSigningDetailsField.get(signatures);
        } catch (Throwable e) {
            return null;
        }
    }

    private void setSigningDetailsOnObject(Object obj, Object signingDetails) {
        try {
            Field signaturesField;
            try {
                signaturesField = obj.getClass().getDeclaredField("signatures");
            } catch (NoSuchFieldException e) {
                signaturesField = obj.getClass().getSuperclass().getDeclaredField("signatures");
                if (signaturesField == null) return;
            }
            signaturesField.setAccessible(true);
            Object signatures = signaturesField.get(obj);
            Field mSigningDetailsField = signatures.getClass().getDeclaredField("mSigningDetails");
            mSigningDetailsField.setAccessible(true);
            mSigningDetailsField.set(signatures, signingDetails);
        } catch (Throwable ignored) {
        }
    }

    private void hookReconcilePackageUtils(ClassLoader classLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        Class<?> reconcileClass = findClass("com.android.server.pm.ReconcilePackageUtils", classLoader);
        if (reconcileClass == null) return;

        try {
            Method reconcilePackages = Arrays.stream(reconcileClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("reconcilePackages")).findFirst().orElse(null);
            if (reconcilePackages != null) {
                deoptimizeMethod(reconcilePackages);
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: reconcilePackages deoptimize failed", e);
        }

        // ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS
        try {
            Field allowField = Arrays.stream(reconcileClass.getDeclaredFields())
                    .filter(f -> f.getName().equals("ALLOW_NON_PRELOADS_SYSTEM_SHAREDUIDS"))
                    .findFirst().orElse(null);
            if (allowField != null) {
                prefs.reload();
                if (prefs.getBoolean("digestCreak", true) && prefs.getBoolean("sharedUser", false)) {
                    allowField.setAccessible(true);
                    allowField.setBoolean(null, true);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void hookVerificationAgent(ClassLoader classLoader) {
        // VerificationParams (SDK33)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            Class<?> vpClass = findClass("com.android.server.pm.VerificationParams", classLoader);
            if (vpClass != null) {
                try {
                    Method isVerificationEnabled = Arrays.stream(vpClass.getDeclaredMethods())
                            .filter(m -> m.getName().equals("isVerificationEnabled")).findFirst().orElse(null);
                    if (isVerificationEnabled != null) {
                        XposedBridge.hookMethod(isVerificationEnabled, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                prefs.reload();
                                if (prefs.getBoolean("disableVerificationAgent", true)) {
                                    param.setResult(false);
                                }
                            }
                        });
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        // VerifyingSession (SDK34+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Class<?> vsClass = findClass("com.android.server.pm.VerifyingSession", classLoader);
            if (vsClass != null) {
                try {
                    Method isVerificationEnabled = Arrays.stream(vsClass.getDeclaredMethods())
                            .filter(m -> m.getName().equals("isVerificationEnabled")).findFirst().orElse(null);
                    if (isVerificationEnabled != null) {
                        XposedBridge.hookMethod(isVerificationEnabled, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                prefs.reload();
                                if (prefs.getBoolean("disableVerificationAgent", true)) {
                                    param.setResult(false);
                                }
                            }
                        });
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void hookInstallPackageHelper(ClassLoader classLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        Class<?> iphClass = findClass("com.android.server.pm.InstallPackageHelper", classLoader);
        if (iphClass == null) return;

        try {
            Method doesSigMatch = Arrays.stream(iphClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("doesSignatureMatchForPermissions")).findFirst().orElse(null);
            if (doesSigMatch != null) {
                XposedBridge.hookMethod(doesSigMatch, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("digestCreak", true) && prefs.getBoolean("UsePreSig", false)) {
                            if (param.getResult().equals(false)) {
                                String pPname = (String) XposedHelpers.callMethod(param.args[1], "getPackageName");
                                if (pPname.contentEquals((String) param.args[0])) {
                                    param.setResult(true);
                                }
                            }
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: InstallPackageHelper hook failed", e);
        }
    }

    private void hookScanPackageUtils(ClassLoader classLoader) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        Class<?> spuClass = findClass("com.android.server.pm.ScanPackageUtils", classLoader);
        if (spuClass == null) return;

        try {
            Method assertMin = Arrays.stream(spuClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals("assertMinSignatureSchemeIsValid")).findFirst().orElse(null);
            if (assertMin != null) {
                XposedBridge.hookMethod(assertMin, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        prefs.reload();
                        if (prefs.getBoolean("authcreak", false)) {
                            param.setResult(null);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            if (DEBUG) Log.e(TAG, "CorePatch: assertMinSignatureSchemeIsValid hook failed", e);
        }
    }

    Class<?> getSigningDetails(ClassLoader classLoader) {
        return XposedHelpers.findClass("android.content.pm.PackageParser.SigningDetails", classLoader);
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        // PackageParser.getApkSigningVersion (legacy)
        hookAllMethods("android.content.pm.PackageParser", null, "getApkSigningVersion",
                XC_MethodReplacement.returnConstant(1));
    }
}
