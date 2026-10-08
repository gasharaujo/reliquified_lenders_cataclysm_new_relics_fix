package dev.codex.rlcnrf;

import dev.codex.rlcnrf.core.LegacyRelicsApiTransformer;
import dev.codex.rlcnrf.legacy.data.cast.misc.CastType;
import dev.codex.rlcnrf.legacy.data.cast.misc.PredicateType;
import dev.codex.rlcnrf.legacy.data.leveling.AbilitiesData;
import dev.codex.rlcnrf.legacy.data.leveling.AbilityData;
import dev.codex.rlcnrf.legacy.data.leveling.LevelingData;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import cpw.mods.modlauncher.api.ITransformer;
import net.neoforged.coremod.CoreModScriptingEngine;
import net.neoforged.coremod.ICoreModScriptSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.ClassNode;

public final class BytecodeCompatibilityVerifier {
    private static final String ADDON_PREFIX = "it/hurts/octostudios/reliquified_lenders_cataclysm/";
    private static final String CURRENT_RELIC_ITEM = "it/hurts/sskirillss/relics/items/relics/base/RelicItem";
    private static final String ADDON_RELIC_ITEM = ADDON_PREFIX + "items/base/RECItem";
    private static final String LEGACY_RELIC_ITEM = "dev/codex/rlcnrf/legacy/LegacyRelicItem";
    private static final String RELIC_ITEM_MIXIN = "dev/codex/rlcnrf/mixin/RelicItemCompatMixin";
    private static final String RELIC_DEFINITION_MIXIN = "dev/codex/rlcnrf/mixin/RelicDefinitionCompatMixin";
    private static final Set<String> RELIC_ITEM_CLASSES = Set.of(
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.back.VoidCloakItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.charm.ScouringEyeItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.charm.VoidVortexInBottleItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.hands.VacuumGloveItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.head.VoidBubbleItem"
    );

    private static final List<String> BANNED = List.of(
        "it/hurts/sskirillss/relics/api/events/common/ContainerSlotClickEvent",
        "it/hurts/sskirillss/relics/client/models/items/CurioModel",
        "it/hurts/sskirillss/relics/client/models/items/SidedCurioModel",
        "it/hurts/sskirillss/relics/components/AbilitiesComponent",
        "it/hurts/sskirillss/relics/components/AbilityComponent",
        "it/hurts/sskirillss/relics/components/LevelingComponent",
        "it/hurts/sskirillss/relics/init/CreativeTabRegistry",
        "it/hurts/sskirillss/relics/init/DataComponentRegistry",
        "it/hurts/sskirillss/relics/init/EffectRegistry",
        "it/hurts/sskirillss/relics/items/relics/base/IRelicItem",
        "it/hurts/sskirillss/relics/items/relics/base/IRenderableCurio",
        "it/hurts/sskirillss/relics/items/relics/base/data/RelicData",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/CastData",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastStage",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastType",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/PredicateType",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilitiesData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilityData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourceData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourcesData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/StatData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/misc/UpgradeOperation",
        "it/hurts/sskirillss/relics/items/relics/base/data/loot/LootData",
        "it/hurts/sskirillss/relics/items/relics/base/data/style/BeamsData",
        "it/hurts/sskirillss/relics/items/relics/base/data/style/StyleData",
        "it/hurts/sskirillss/relics/items/relics/base/data/style/TooltipData",
        "it/hurts/sskirillss/relics/network/packets/PacketPlayerMotion"
    );

    private BytecodeCompatibilityVerifier() {
    }

    private static int checkedReferences;
    private static int checkedAddonHierarchyReferences;
    private static final Map<String, ClassNode> CLASS_CACHE = new HashMap<>();
    private static final Set<String> RESOLVED_API_TYPES = new LinkedHashSet<>();

    private record TransformedClass(String className, ClassNode node, byte[] bytes) {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected add-on JAR and compatibility JAR");
        Path addon = Path.of(args[0]);
        Path compat = Path.of(args[1]);
        ITransformer<ClassNode> transformer = loadCoreModTransformer(compat);
        Set<String> targets = transformer.targets().stream().map(target -> target.className()).collect(java.util.stream.Collectors.toSet());
        Set<String> javaReferenceTargets = new LegacyRelicsApiTransformer().targets().stream()
            .map(target -> target.className())
            .collect(java.util.stream.Collectors.toSet());
        if (!targets.equals(javaReferenceTargets)) {
            throw new AssertionError("JavaScript coremod targets differ from the reference transformer: " + targets);
        }

        int classCount = 0;
        int transformedCount = 0;
        Set<String> seenTargets = new HashSet<>();
        List<TransformedClass> transformedClasses = new ArrayList<>();
        try (ZipFile archive = new ZipFile(addon.toFile())) {
            var entries = archive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                classCount++;
                byte[] bytes = archive.getInputStream(entry).readAllBytes();
                ClassNode node = new ClassNode();
                new ClassReader(bytes).accept(node, 0);
                String className = node.name.replace('/', '.');
                if (targets.contains(className)) {
                    seenTargets.add(className);
                    transformer.transform(node, null);
                    transformedCount++;
                }
                ClassWriter writer = new ClassWriter(0);
                node.accept(writer);
                bytes = writer.toByteArray();
                CLASS_CACHE.put(node.name, node);
                transformedClasses.add(new TransformedClass(className, node, bytes));
            }
        }

        if (!seenTargets.equals(targets)) {
            Set<String> missing = new HashSet<>(targets);
            missing.removeAll(seenTargets);
            throw new AssertionError("Transformer targets absent from add-on JAR: " + missing);
        }

        for (TransformedClass transformed : transformedClasses) {
            verifyMemberReferences(transformed.node());
            verifyReferencedTypes(transformed.node());
            assertNoBannedReferences(transformed.className(), transformed.bytes());
        }
        verifyConcreteLegacyForwarders();
        verifyRelicDefinitionTargets();
        verifyBridgeSemantics();
        verifyCompatJar(compat);
        System.out.printf(
            "Verified %d add-on classes; transformed %d affected classes; resolved %d Relics/bridge member references, "
                + "%d add-on hierarchy calls, and %d referenced API types; no removed Relics symbols remain.%n",
            classCount, transformedCount, checkedReferences, checkedAddonHierarchyReferences, RESOLVED_API_TYPES.size()
        );
    }

    @SuppressWarnings("unchecked")
    private static ITransformer<ClassNode> loadCoreModTransformer(Path compat) throws IOException {
        CoreModScriptingEngine engine = new CoreModScriptingEngine();
        engine.loadCoreMod(new JarScriptSource(compat));
        List<ITransformer<?>> transformers = engine.initializeCoreMods();
        if (transformers.size() != 1) {
            throw new AssertionError("Expected one JavaScript coremod transformer, found " + transformers.size());
        }
        ITransformer<?> transformer = transformers.getFirst();
        if (!transformer.getTargetType().equals(cpw.mods.modlauncher.api.TargetType.CLASS)) {
            throw new AssertionError("JavaScript coremod did not create a class transformer");
        }
        return (ITransformer<ClassNode>) transformer;
    }

    private record JarScriptSource(Path jar) implements ICoreModScriptSource {
        @Override
        public String getOwnerId() {
            return "reliquified_lenders_cataclysm_new_relics_fix";
        }

        @Override
        public Reader readCoreMod() throws IOException {
            return readEntry("coremods/legacy_relics_api.js");
        }

        @Override
        public Reader getAdditionalFile(String name) throws IOException {
            return readEntry(name);
        }

        @Override
        public String getDebugSource() {
            return jar + "!/coremods/legacy_relics_api.js";
        }

        private Reader readEntry(String name) throws IOException {
            try (ZipFile archive = new ZipFile(jar.toFile())) {
                ZipEntry entry = archive.getEntry(name);
                if (entry == null) throw new IOException("Missing coremod resource: " + name);
                return new StringReader(new String(archive.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }

    private static void verifyMemberReferences(ClassNode node) throws Exception {
        for (var method : node.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && isAuditedOwner(call.owner)) {
                    if (!hasMethod(call.owner, call.name, call.desc, new HashSet<>())) {
                        throw new AssertionError(node.name + " calls missing transformed method " + call.owner + "." + call.name + call.desc);
                    }
                    checkedReferences++;
                    if (call.owner.startsWith(ADDON_PREFIX)) checkedAddonHierarchyReferences++;
                } else if (instruction instanceof FieldInsnNode access && isAuditedOwner(access.owner)) {
                    if (!hasField(access.owner, access.name, access.desc, new HashSet<>())) {
                        throw new AssertionError(node.name + " accesses missing transformed field " + access.owner + "." + access.name + access.desc);
                    }
                    checkedReferences++;
                    if (access.owner.startsWith(ADDON_PREFIX)) checkedAddonHierarchyReferences++;
                }
            }
        }
    }

    private static boolean isRelicsOwner(String owner) {
        return owner.startsWith("dev/codex/rlcnrf/legacy/")
            || owner.startsWith("it/hurts/sskirillss/relics/");
    }

    private static boolean isAuditedOwner(String owner) {
        return isRelicsOwner(owner) || owner.startsWith(ADDON_PREFIX);
    }

    private static boolean hasMethod(String internalName, String name, String descriptor, Set<String> visited) throws IOException {
        if (internalName == null || !visited.add(internalName)) return false;
        if (internalName.equals(ADDON_RELIC_ITEM)) {
            MethodNode bridge = findDeclaredMethod(readClass(RELIC_ITEM_MIXIN), name, descriptor);
            if (bridge != null && (bridge.access & Opcodes.ACC_ABSTRACT) == 0) return true;
            if (hasMethod(LEGACY_RELIC_ITEM, name, descriptor, visited)) return true;
        }
        ClassNode type = readClass(internalName);
        if (findDeclaredMethod(type, name, descriptor) != null) return true;
        if (name.equals("<init>")) return false;
        if (hasMethod(type.superName, name, descriptor, visited)) return true;
        for (String contract : type.interfaces) {
            if (hasMethod(contract, name, descriptor, visited)) return true;
        }
        return false;
    }

    private static MethodNode findDeclaredMethod(ClassNode type, String name, String descriptor) {
        for (var method : type.methods) {
            if (method.name.equals(name) && method.desc.equals(descriptor)) return method;
        }
        return null;
    }

    private static boolean hasField(String internalName, String name, String descriptor, Set<String> visited) throws IOException {
        if (internalName == null || !visited.add(internalName)) return false;
        ClassNode type = readClass(internalName);
        for (var field : type.fields) {
            if (field.name.equals(name) && field.desc.equals(descriptor)) return true;
        }
        if (hasField(type.superName, name, descriptor, visited)) return true;
        for (String contract : type.interfaces) {
            if (hasField(contract, name, descriptor, visited)) return true;
        }
        return false;
    }

    private static ClassNode readClass(String internalName) throws IOException {
        ClassNode cached = CLASS_CACHE.get(internalName);
        if (cached != null) return cached;
        try (InputStream stream = BytecodeCompatibilityVerifier.class.getClassLoader().getResourceAsStream(internalName + ".class")) {
            if (stream == null) throw new AssertionError("Mapped owner class is absent: " + internalName);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            CLASS_CACHE.put(internalName, node);
            return node;
        }
    }

    private static void verifyReferencedTypes(ClassNode node) throws IOException {
        recordTypeName(node.superName);
        for (String contract : node.interfaces) recordTypeName(contract);
        recordTypeName(node.outerClass);
        recordTypeName(node.nestHostClass);
        if (node.nestMembers != null) for (String member : node.nestMembers) recordTypeName(member);
        if (node.permittedSubclasses != null) for (String subtype : node.permittedSubclasses) recordTypeName(subtype);

        for (var field : node.fields) recordDescriptor(field.desc);
        for (var method : node.methods) {
            recordDescriptor(method.desc);
            if (method.exceptions != null) for (String exception : method.exceptions) recordTypeName(exception);
            for (var instruction : method.instructions) {
                if (instruction instanceof TypeInsnNode typeInstruction) {
                    if (typeInstruction.desc.startsWith("[")) recordDescriptor(typeInstruction.desc);
                    else recordTypeName(typeInstruction.desc);
                } else if (instruction instanceof FieldInsnNode fieldInstruction) {
                    recordTypeName(fieldInstruction.owner);
                    recordDescriptor(fieldInstruction.desc);
                } else if (instruction instanceof MethodInsnNode methodInstruction) {
                    recordTypeName(methodInstruction.owner);
                    recordDescriptor(methodInstruction.desc);
                } else if (instruction instanceof InvokeDynamicInsnNode dynamicInstruction) {
                    recordDescriptor(dynamicInstruction.desc);
                    recordHandle(dynamicInstruction.bsm);
                    for (Object argument : dynamicInstruction.bsmArgs) recordConstant(argument);
                } else if (instruction instanceof LdcInsnNode constantInstruction) {
                    recordConstant(constantInstruction.cst);
                } else if (instruction instanceof MultiANewArrayInsnNode arrayInstruction) {
                    recordDescriptor(arrayInstruction.desc);
                } else if (instruction instanceof FrameNode frame) {
                    recordFrame(frame.local);
                    recordFrame(frame.stack);
                }
            }
            if (method.tryCatchBlocks != null) {
                for (var block : method.tryCatchBlocks) recordTypeName(block.type);
            }
        }
    }

    private static void recordFrame(List<Object> entries) throws IOException {
        if (entries == null) return;
        for (Object entry : entries) if (entry instanceof String name) recordTypeName(name);
    }

    private static void recordConstant(Object value) throws IOException {
        if (value instanceof Type type) {
            recordType(type);
        } else if (value instanceof Handle handle) {
            recordHandle(handle);
        } else if (value instanceof ConstantDynamic dynamic) {
            recordDescriptor(dynamic.getDescriptor());
            recordHandle(dynamic.getBootstrapMethod());
            for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++) {
                recordConstant(dynamic.getBootstrapMethodArgument(i));
            }
        }
    }

    private static void recordHandle(Handle handle) throws IOException {
        recordTypeName(handle.getOwner());
        recordDescriptor(handle.getDesc());
    }

    private static void recordDescriptor(String descriptor) throws IOException {
        if (descriptor == null) return;
        recordType(Type.getType(descriptor));
    }

    private static void recordType(Type type) throws IOException {
        if (type.getSort() == Type.METHOD) {
            recordType(type.getReturnType());
            for (Type argument : type.getArgumentTypes()) recordType(argument);
        } else if (type.getSort() == Type.ARRAY) {
            recordType(type.getElementType());
        } else if (type.getSort() == Type.OBJECT) {
            recordTypeName(type.getInternalName());
        }
    }

    private static void recordTypeName(String internalName) throws IOException {
        if (internalName == null || !isRelicsOwner(internalName) || !RESOLVED_API_TYPES.add(internalName)) return;
        readClass(internalName);
    }

    private static void verifyConcreteLegacyForwarders() throws IOException {
        ClassNode contract = readClass(LEGACY_RELIC_ITEM);
        ClassNode mixin = readClass(RELIC_ITEM_MIXIN);
        Set<String> annotatedTargets = new HashSet<>();
        readMixinTargets(mixin.visibleAnnotations, annotatedTargets);
        readMixinTargets(mixin.invisibleAnnotations, annotatedTargets);
        if (!annotatedTargets.equals(Set.of(ADDON_RELIC_ITEM.replace('/', '.')))) {
            throw new AssertionError("Legacy forwarders must target only RECItem: " + annotatedTargets);
        }
        if (annotatedTargets.contains(CURRENT_RELIC_ITEM.replace('/', '.'))) {
            throw new AssertionError("Cataclysm bridge leaked onto the global RelicItem base");
        }
        int forwarders = 0;
        for (MethodNode legacyMethod : contract.methods) {
            if ((legacyMethod.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_ABSTRACT)) != 0) continue;
            MethodNode implementation = findDeclaredMethod(mixin, legacyMethod.name, legacyMethod.desc);
            if (implementation == null || (implementation.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT)) != 0) {
                throw new AssertionError("RECItem mixin lacks concrete legacy forwarder "
                    + legacyMethod.name + legacyMethod.desc);
            }
            boolean delegatesToContract = false;
            for (var instruction : implementation.instructions) {
                if (instruction instanceof MethodInsnNode call
                    && call.owner.equals(LEGACY_RELIC_ITEM)
                    && call.name.equals(legacyMethod.name)
                    && call.desc.equals(legacyMethod.desc)) {
                    delegatesToContract = true;
                    break;
                }
            }
            if (!delegatesToContract) {
                throw new AssertionError("Concrete legacy forwarder does not delegate to its bridge default: "
                    + legacyMethod.name + legacyMethod.desc);
            }
            forwarders++;
        }
        if (forwarders < 30) {
            throw new AssertionError("Unexpectedly small concrete legacy surface: " + forwarders);
        }

        String curiosAttributes = "(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/resources/ResourceLocation;"
            + "Lnet/minecraft/world/item/ItemStack;)Lcom/google/common/collect/Multimap;";
        MethodNode attributes = findDeclaredMethod(mixin, "getAttributeModifiers", curiosAttributes);
        if (attributes == null || (attributes.access & Opcodes.ACC_ABSTRACT) != 0) {
            throw new AssertionError("Curios ResourceLocation attribute bridge must be concrete on RECItem");
        }
        MethodNode activation = findDeclaredMethod(
            mixin, "activateAbility",
            "(Lit/hurts/sskirillss/relics/api/relics/abilities/activation/AbilityActivationContext;)V"
        );
        if (activation == null || (activation.access & Opcodes.ACC_ABSTRACT) != 0) {
            throw new AssertionError("Current Relics ability activation bridge must be concrete on RECItem");
        }
    }

    private static void verifyRelicDefinitionTargets() throws IOException {
        ClassNode mixin = readClass(RELIC_DEFINITION_MIXIN);
        MethodNode definition = findDeclaredMethod(
            mixin, "constructDefaultRelicTemplate",
            "()Lit/hurts/sskirillss/relics/api/relics/RelicTemplate;"
        );
        if (definition == null || (definition.access & Opcodes.ACC_ABSTRACT) != 0) {
            throw new AssertionError("Relic definition translator must be concrete");
        }

        Set<String> annotatedTargets = new HashSet<>();
        readMixinTargets(mixin.visibleAnnotations, annotatedTargets);
        readMixinTargets(mixin.invisibleAnnotations, annotatedTargets);
        if (!annotatedTargets.equals(RELIC_ITEM_CLASSES)) {
            throw new AssertionError("Relic definition mixin targets differ from the five add-on relics: " + annotatedTargets);
        }

        for (String target : RELIC_ITEM_CLASSES) {
            ClassNode item = CLASS_CACHE.get(target.replace('.', '/'));
            if (item == null) throw new AssertionError("Missing transformed relic item " + target);
            MethodNode legacyDefinition = findDeclaredMethod(
                item, "constructDefaultRelicData", "()Ldev/codex/rlcnrf/legacy/data/RelicData;"
            );
            if (legacyDefinition == null || (legacyDefinition.access & Opcodes.ACC_ABSTRACT) != 0) {
                throw new AssertionError("Legacy relic definition was not transformed for " + target);
            }
        }
    }

    private static void readMixinTargets(List<AnnotationNode> annotations, Set<String> targets) {
        if (annotations == null) return;
        for (AnnotationNode annotation : annotations) {
            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;") || annotation.values == null) continue;
            for (int i = 0; i < annotation.values.size(); i += 2) {
                if (!annotation.values.get(i).equals("targets")) continue;
                Object value = annotation.values.get(i + 1);
                if (value instanceof List<?> values) {
                    for (Object target : values) targets.add(String.valueOf(target));
                }
            }
        }
    }

    private static void verifyBridgeSemantics() throws IOException {
        MethodNode levelingConversion = findDeclaredMethod(
            readClass("dev/codex/rlcnrf/legacy/data/leveling/LevelingData"),
            "toTemplate", "()Lit/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingTemplate;"
        );
        Set<String> levelingBuilderCalls = new HashSet<>();
        for (var instruction : levelingConversion.instructions) {
            if (instruction instanceof MethodInsnNode call
                && call.owner.equals("it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingTemplate$LevelingTemplateBuilder")) {
                levelingBuilderCalls.add(call.name + call.desc);
            }
        }
        Set<String> requiredLevelingCalls = Set.of(
            "initialCost(D)Lit/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingTemplate$LevelingTemplateBuilder;",
            "step(D)Lit/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingTemplate$LevelingTemplateBuilder;",
            "maxRank(I)Lit/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingTemplate$LevelingTemplateBuilder;"
        );
        if (!levelingBuilderCalls.containsAll(requiredLevelingCalls)) {
            throw new AssertionError("Legacy leveling conversion drops current Relics fields: " + levelingBuilderCalls);
        }

        var abilities = AbilitiesData.builder()
            .ability(AbilityData.builder("first").build())
            .ability(AbilityData.builder("second").build())
            .build();
        if (!List.copyOf(abilities.getAbilities().keySet()).equals(List.of("first", "second"))) {
            throw new AssertionError("Legacy ability order is not preserved");
        }

        for (CastType type : CastType.values()) {
            if (!type.name().equals(type.toCurrent().name())) {
                throw new AssertionError("Legacy cast type changed meaning: " + type);
            }
        }
        for (PredicateType type : PredicateType.values()) {
            if (!type.name().equals(type.toCurrent().name())) {
                throw new AssertionError("Legacy predicate type changed meaning: " + type);
            }
        }
    }

    private static void assertNoBannedReferences(String className, byte[] bytes) {
        String constants = new String(bytes, StandardCharsets.ISO_8859_1);
        for (String banned : BANNED) {
            if (constants.contains(banned)) {
                throw new AssertionError(className + " still references removed symbol " + banned);
            }
        }
    }

    private static void verifyCompatJar(Path compat) throws IOException {
        try (ZipFile archive = new ZipFile(compat.toFile())) {
            requireEntry(archive, "META-INF/coremods.json");
            requireEntry(archive, "coremods/legacy_relics_api.js");
            requireEntry(archive, "META-INF/neoforge.mods.toml");
            requireEntry(archive, "reliquified_lenders_cataclysm_new_relics_fix.mixins.json");
            requireEntry(archive, "dev/codex/rlcnrf/legacy/LegacyEffectRegistry.class");
            requireEntry(archive, "dev/codex/rlcnrf/legacy/LegacyRelicItem.class");
            requireEntry(archive, "dev/codex/rlcnrf/legacy/network/LegacyPacketPlayerMotion.class");
            requireEntry(archive, "dev/codex/rlcnrf/mixin/RelicItemCompatMixin.class");
            requireEntry(archive, "dev/codex/rlcnrf/mixin/RelicDefinitionCompatMixin.class");
            requireEntry(archive, "dev/codex/rlcnrf/mixin/ScouringEyeCooldownMixin.class");
            requireEntry(archive, "dev/codex/rlcnrf/mixin/VoidCloakSafetyMixin.class");
            verifyGameplayFixes(archive);
            var entries = archive.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("it/hurts/sskirillss/relics/")) {
                    throw new AssertionError("Compatibility JAR creates a split Relics package: " + name);
                }
            }
        }
    }

    private static void verifyGameplayFixes(ZipFile archive) throws IOException {
        ClassNode cooldownMixin = readArchiveClass(archive, "dev/codex/rlcnrf/mixin/ScouringEyeCooldownMixin.class");
        String cooldownSymbols = classSymbols(cooldownMixin);
        for (String required : List.of("glowing_scour", "getActivationCooldown", "addActivationCooldown")) {
            if (!cooldownSymbols.contains(required)) {
                throw new AssertionError("Pursuit cooldown fix is missing " + required);
            }
        }

        ClassNode voidMixin = readArchiveClass(archive, "dev/codex/rlcnrf/mixin/VoidCloakSafetyMixin.class");
        String voidSymbols = classSymbols(voidMixin);
        for (String required : List.of(
            "void_cloak_safe_position", "void_cloak_safe_dimension", "getPersistentData",
            "teleportTo", "resetFallDistance", "setCanceled"
        )) {
            if (!voidSymbols.contains(required)) {
                throw new AssertionError("Void Cloak rescue fix is missing " + required);
            }
        }
    }

    private static ClassNode readArchiveClass(ZipFile archive, String name) throws IOException {
        ZipEntry entry = archive.getEntry(name);
        if (entry == null) throw new AssertionError("Missing JAR entry: " + name);
        ClassNode node = new ClassNode();
        new ClassReader(archive.getInputStream(entry).readAllBytes()).accept(node, 0);
        return node;
    }

    private static String classSymbols(ClassNode node) {
        StringBuilder symbols = new StringBuilder();
        for (var method : node.methods) {
            symbols.append(method.name).append(method.desc);
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call) {
                    symbols.append(call.owner).append(call.name).append(call.desc);
                } else if (instruction instanceof LdcInsnNode constant) {
                    symbols.append(constant.cst);
                }
            }
        }
        return symbols.toString();
    }

    private static void requireEntry(ZipFile archive, String name) {
        if (archive.getEntry(name) == null) throw new AssertionError("Missing JAR entry: " + name);
    }
}
