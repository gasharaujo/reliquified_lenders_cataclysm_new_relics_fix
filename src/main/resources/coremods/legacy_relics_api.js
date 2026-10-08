var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI');
var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
var Handle = Java.type('org.objectweb.asm.Handle');
var Type = Java.type('org.objectweb.asm.Type');

var relicsPrefix = 'it/hurts/sskirillss/relics/';
var typeMap = {
    'api/events/common/ContainerSlotClickEvent': 'it/hurts/sskirillss/relics/api/events/utility/ContainerSlotClickEvent',
    'client/models/items/CurioModel': 'dev/codex/rlcnrf/legacy/client/LegacyCurioModel',
    'client/models/items/SidedCurioModel': 'dev/codex/rlcnrf/legacy/client/LegacySidedCurioModel',
    'components/AbilitiesComponent': 'dev/codex/rlcnrf/legacy/component/LegacyAbilitiesComponent',
    'components/AbilityComponent': 'dev/codex/rlcnrf/legacy/component/LegacyAbilityComponent',
    'components/LevelingComponent': 'dev/codex/rlcnrf/legacy/component/LegacyLevelingComponent',
    'init/CreativeTabRegistry': 'it/hurts/sskirillss/relics/init/RelicsCreativeTabs',
    'init/DataComponentRegistry': 'dev/codex/rlcnrf/legacy/LegacyDataComponentRegistry',
    'init/EffectRegistry': 'dev/codex/rlcnrf/legacy/LegacyEffectRegistry',
    'items/relics/base/IRelicItem': 'dev/codex/rlcnrf/legacy/LegacyRelicItem',
    'items/relics/base/IRenderableCurio': 'dev/codex/rlcnrf/legacy/client/LegacyRenderableCurio',
    'items/relics/base/data/RelicData': 'dev/codex/rlcnrf/legacy/data/RelicData',
    'items/relics/base/data/cast/CastData': 'dev/codex/rlcnrf/legacy/data/cast/CastData',
    'items/relics/base/data/cast/misc/CastStage': 'dev/codex/rlcnrf/legacy/data/cast/misc/CastStage',
    'items/relics/base/data/cast/misc/CastType': 'dev/codex/rlcnrf/legacy/data/cast/misc/CastType',
    'items/relics/base/data/cast/misc/PredicateType': 'dev/codex/rlcnrf/legacy/data/cast/misc/PredicateType',
    'items/relics/base/data/leveling/AbilitiesData': 'dev/codex/rlcnrf/legacy/data/leveling/AbilitiesData',
    'items/relics/base/data/leveling/AbilityData': 'dev/codex/rlcnrf/legacy/data/leveling/AbilityData',
    'items/relics/base/data/leveling/LevelingData': 'dev/codex/rlcnrf/legacy/data/leveling/LevelingData',
    'items/relics/base/data/leveling/LevelingSourceData': 'dev/codex/rlcnrf/legacy/data/leveling/LevelingSourceData',
    'items/relics/base/data/leveling/LevelingSourcesData': 'dev/codex/rlcnrf/legacy/data/leveling/LevelingSourcesData',
    'items/relics/base/data/leveling/StatData': 'dev/codex/rlcnrf/legacy/data/leveling/StatData',
    'items/relics/base/data/leveling/misc/UpgradeOperation': 'dev/codex/rlcnrf/legacy/data/leveling/misc/UpgradeOperation',
    'items/relics/base/data/loot/LootData': 'dev/codex/rlcnrf/legacy/data/loot/LootData',
    'items/relics/base/data/style/BeamsData': 'dev/codex/rlcnrf/legacy/data/style/BeamsData',
    'items/relics/base/data/style/StyleData': 'dev/codex/rlcnrf/legacy/data/style/StyleData',
    'items/relics/base/data/style/TooltipData': 'dev/codex/rlcnrf/legacy/data/style/TooltipData',
    'network/packets/PacketPlayerMotion': 'dev/codex/rlcnrf/legacy/network/LegacyPacketPlayerMotion'
};

function mapText(value) {
    if (value === null || value === undefined) return value;
    var mapped = String(value);
    var suffixes = Object.keys(typeMap);
    for (var i = 0; i < suffixes.length; i++) {
        var oldName = relicsPrefix + suffixes[i];
        mapped = mapped.split(oldName).join(typeMap[suffixes[i]]);
    }
    return mapped;
}

function mapHandle(handle) {
    return new Handle(
        handle.getTag(),
        mapText(handle.getOwner()),
        handle.getName(),
        mapText(handle.getDesc()),
        handle.isInterface()
    );
}

function mapConstant(value) {
    if (value instanceof Type) return Type.getType(mapText(value.getDescriptor()));
    if (value instanceof Handle) return mapHandle(value);
    return value;
}

function mapStringList(list) {
    if (list === null || list === undefined) return;
    for (var i = 0; i < list.size(); i++) {
        list.set(i, mapText(list.get(i)));
    }
}

function mapFrameList(list) {
    if (list === null || list === undefined) return;
    for (var i = 0; i < list.size(); i++) {
        var value = list.get(i);
        if (typeof value === 'string' || (value !== null && typeof value.charAt === 'function')) {
            list.set(i, mapText(value));
        }
    }
}

function remapClass(classNode) {
    var replacements = 0;

    function mapped(value) {
        var result = mapText(value);
        if (value !== null && value !== undefined && String(value) !== String(result)) replacements++;
        return result;
    }

    classNode.superName = mapped(classNode.superName);
    mapStringList(classNode.interfaces);
    classNode.signature = mapped(classNode.signature);
    classNode.outerClass = mapped(classNode.outerClass);
    classNode.outerMethodDesc = mapped(classNode.outerMethodDesc);
    classNode.nestHostClass = mapped(classNode.nestHostClass);
    mapStringList(classNode.nestMembers);
    mapStringList(classNode.permittedSubclasses);

    if (classNode.innerClasses !== null) {
        for (var innerIndex = 0; innerIndex < classNode.innerClasses.size(); innerIndex++) {
            var inner = classNode.innerClasses.get(innerIndex);
            inner.name = mapped(inner.name);
            inner.outerName = mapped(inner.outerName);
        }
    }

    if (classNode.recordComponents !== null) {
        for (var recordIndex = 0; recordIndex < classNode.recordComponents.size(); recordIndex++) {
            var component = classNode.recordComponents.get(recordIndex);
            component.descriptor = mapped(component.descriptor);
            component.signature = mapped(component.signature);
        }
    }

    for (var fieldIndex = 0; fieldIndex < classNode.fields.size(); fieldIndex++) {
        var field = classNode.fields.get(fieldIndex);
        field.desc = mapped(field.desc);
        field.signature = mapped(field.signature);
        field.value = mapConstant(field.value);
    }

    for (var methodIndex = 0; methodIndex < classNode.methods.size(); methodIndex++) {
        var method = classNode.methods.get(methodIndex);
        method.desc = mapped(method.desc);
        method.signature = mapped(method.signature);
        mapStringList(method.exceptions);

        var instructions = method.instructions;
        for (var instructionIndex = 0; instructionIndex < instructions.size(); instructionIndex++) {
            var instruction = instructions.get(instructionIndex);
            switch (instruction.getType()) {
                case AbstractInsnNode.TYPE_INSN:
                    instruction.desc = mapped(instruction.desc);
                    break;
                case AbstractInsnNode.FIELD_INSN:
                    instruction.owner = mapped(instruction.owner);
                    instruction.desc = mapped(instruction.desc);
                    break;
                case AbstractInsnNode.METHOD_INSN:
                    instruction.owner = mapped(instruction.owner);
                    instruction.desc = mapped(instruction.desc);
                    break;
                case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                    instruction.desc = mapped(instruction.desc);
                    instruction.bsm = mapHandle(instruction.bsm);
                    for (var argumentIndex = 0; argumentIndex < instruction.bsmArgs.length; argumentIndex++) {
                        instruction.bsmArgs[argumentIndex] = mapConstant(instruction.bsmArgs[argumentIndex]);
                    }
                    break;
                case AbstractInsnNode.LDC_INSN:
                    instruction.cst = mapConstant(instruction.cst);
                    break;
                case AbstractInsnNode.MULTIANEWARRAY_INSN:
                    instruction.desc = mapped(instruction.desc);
                    break;
                case AbstractInsnNode.FRAME:
                    mapFrameList(instruction.local);
                    mapFrameList(instruction.stack);
                    break;
            }
        }

        if (method.tryCatchBlocks !== null) {
            for (var catchIndex = 0; catchIndex < method.tryCatchBlocks.size(); catchIndex++) {
                var catchBlock = method.tryCatchBlocks.get(catchIndex);
                catchBlock.type = mapped(catchBlock.type);
            }
        }
        if (method.localVariables !== null) {
            for (var localIndex = 0; localIndex < method.localVariables.size(); localIndex++) {
                var local = method.localVariables.get(localIndex);
                local.desc = mapped(local.desc);
                local.signature = mapped(local.signature);
            }
        }
    }

    ASMAPI.log('INFO', 'Remapped {} legacy Relics references in {}', replacements, classNode.name);
    return classNode;
}

function initializeCoreMod() {
    return {
        'legacy_relics_api': {
            'target': {
                'type': 'CLASS',
                'names': function(listOfClasses) {
                    return [
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.entities.VoidVortexModifiedEntity',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.items.base.RECItem',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.back.VoidCloakItem',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.charm.ScouringEyeItem',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.charm.VoidVortexInBottleItem',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.hands.VacuumGloveItem',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.head.VoidBubbleItem',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.utils.ItemUtils',
                        'it.hurts.octostudios.reliquified_lenders_cataclysm.utils.relics.ScouringEyeUtils'
                    ];
                }
            },
            'transformer': remapClass
        }
    };
}
